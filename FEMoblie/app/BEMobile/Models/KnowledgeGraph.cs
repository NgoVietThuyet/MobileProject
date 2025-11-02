namespace BEMobile.Models
{
    public class KnowledgeGraph
    {
        public class ExtractedEntity
        {
            public string Name { get; set; } = string.Empty;
            public string Type { get; set; } = string.Empty;
        }

        public class ExtractedRelationship
        {
            public string SourceEntityName { get; set; } = string.Empty;
            public string RelationshipType { get; set; } = string.Empty;
            public string TargetEntityName { get; set; } = string.Empty;
        }

        public class ExtractedGraph
        {
            public List<ExtractedEntity> Entities { get; set; } = new List<ExtractedEntity>();
            public List<ExtractedRelationship> Relationships { get; set; } = new List<ExtractedRelationship>();
        }
    }
}
