import os
import re
import textwrap
import unicodedata
from dotenv import load_dotenv

import google.generativeai as genai
from langchain_community.graphs import Neo4jGraph
from langchain.chains import GraphCypherQAChain
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain.prompts import PromptTemplate
from langchain_core.runnables import RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser

# Tải các biến môi trường từ file .env
load_dotenv()

NEO4J_URI = os.getenv("NEO4J_URI")
NEO4J_USERNAME = os.getenv("NEO4J_USERNAME")
NEO4J_PASSWORD = os.getenv("NEO4J_PASSWORD")
NEO4J_DATABASE = "neo4j"

API_KEY = os.getenv("GEMINI_API_KEY")

# Khởi tạo đối tượng đồ thị Neo4j
try:
    kg = Neo4jGraph(
        url=NEO4J_URI,
        username=NEO4J_USERNAME,
        password=NEO4J_PASSWORD,
        database=NEO4J_DATABASE
    )
    print("Kết nối Neo4j thành công.")
except Exception as e:
    print(f"Lỗi kết nối Neo4j: {e}")
    exit()


def extract_entities_and_relationships(text: str) -> str:
    """Gửi văn bản đến Gemini API để trích xuất thực thể và mối quan hệ."""
    genai.configure(api_key=API_KEY)

    prompt = (
        f"Extract entities (nodes) and their relationships (edges) from the text below.\n"
        f"Entities and relationships MUST be in Vietnamese\n"
        f"Follow this format:\n\n"
        f"- {{Entity}}: {{Type}}\n\n"
        f"Relationships:\n"
        f"- ({{Entity1}}, {{RelationshipType}}, {{Entity2}})\n\n"
        f"Text:\n\"{text}\"\n\n"
        f"Ouput:\nEntities:\n- {{Entity}}: {{Type}}\n...\n\n"
        f"Relationships:\n- ({{Entity1}}, {{RelationshipType}}, {{Entity2}})\n"
    )

    try:
        model = genai.GenerativeModel("gemini-2.0-flash")
        response = model.generate_content(prompt)
        return response.text.strip()
    except Exception as e:
        raise RuntimeError(f"Lỗi khi gọi Gemini API: {e}")

def process_llm_output(response: str) -> tuple:
    """Phân tích văn bản trả về từ LLM thành danh sách thực thể và quan hệ."""
    entity_pattern = r"- (.+): (.+)"
    entities = re.findall(entity_pattern, response)
    entity_dict = {entity.strip(): entity_type.strip() for entity, entity_type in entities}

    relationship_pattern = r"- \(([^,]+), ([^,]+), ([^,)]+)\)"
    relationships = re.findall(relationship_pattern, response)
    relationship_list = [(subject.strip(), relation.strip(), object_.strip()) for subject, relation, object_ in relationships]

    print("Thực thể (Entities):")
    for entity, entity_type in entity_dict.items():
        print(f"  - {entity} (Type: {entity_type})")
    print("\nMối quan hệ (Relationships):")
    for subject, relation, object_ in relationship_list:
        print(f"  - ({subject}) -> [{relation}] -> ({object_})")

    return entity_dict, relationship_list

def sanitize_rel(raw: str) -> str:
    """Chuẩn hóa tên quan hệ: viết hoa, giữ dấu, thay khoảng trắng bằng gạch dưới."""
    if not raw:
        return "RELATED_TO"
    s = str(raw).strip().replace(" ", "_").upper()
    return s

def add_relationships_to_neo4j(graph: Neo4jGraph, relationships: list, entities: dict):
    """Thêm các thực thể và mối quan hệ vào Neo4j."""
    for subject, relation_original, obj in relationships:
        subject_type = entities.get(subject, 'Entity')
        object_type = entities.get(obj, 'Entity')
        rel_type = sanitize_rel(relation_original)
        
        cypher = f"""
        MERGE (a:`{subject_type}` {{name: $subject}})
        MERGE (b:`{object_type}` {{name: $object}})
        MERGE (a)-[r:`{rel_type}`]->(b)
        ON CREATE SET r.label = $relation_label
        ON MATCH SET r.label = $relation_label
        """
        params = {
            "subject": subject,
            "object": obj,
            "relation_label": relation_original,
        }
        graph.query(cypher, params=params)
    print("Cập nhật hoàn tất.")


def main():
    Sample_data = [
        "UserId: 1 * Name: Mai Đức Văn * Email: abc123@gmail.com * Facebook: vandeptrai * PhoneNumber: 0123456789 * CreatedDate: 23/05/2025 * TransactionID: 1, 3, 4, 5, 6",
        "TransactionId: 1 * Type: expense * Amount: 400000 * Note: null * CreatedDate: 28/05/2025 * UpdatedDate: 28/05/2025 * CategoryID: 1",
        "TransactionId: 3 * Type: expense * Amount: 20000 * Note: Kem đánh răng * CreatedDate: 28/05/2025 * UpdatedDate: 28/05/2025 * CategoryID: 1",
        "TransactionId: 4 * Type: expense * Amount: 10000 * Note: Thuốc nhỏ mắt * CreatedDate: 28/05/2025 * UpdatedDate: 28/05/2025 * CategoryID: 1",
        "TransactionId: 5 * Type: expense * Amount: 200000 * Note: Bỉm * CreatedDate: 28/05/2025 * UpdatedDate: 28/05/2025 * CategoryID: 2",
        "TransactionId: 6 * Type: expense * Amount: 1000000 * Note: Sữa * CreatedDate: 28/05/2025 * UpdatedDate: 28/05/2025 * CategoryID: 2",
        "CategoryID: 1 * CategoryName: Đồ sinh hoạt",
        "CategoryID: 2 * CategoryName: Chăm con"
    ]


    # Tạo vòng lặp để xử lý từng dòng trong Sample_data
    for i, line in enumerate(Sample_data):

        llm_result = extract_entities_and_relationships(line)
        entities_dict, relationship_list = process_llm_output(llm_result)
        if not relationship_list:
            print("Không tìm thấy mối quan hệ nào, bỏ qua việc thêm vào đồ thị.")
            continue
        add_relationships_to_neo4j(kg, relationship_list, entities_dict)



if __name__ == "__main__":
    main()