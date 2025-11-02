package com.example.test.utils

object PasswordValidator {
    data class ValidationResult(
        val isValid: Boolean,
        val message: String
    )

    fun validate(password: String): ValidationResult {
        if (password.length < 8) {
            return ValidationResult(false, "Mật khẩu phải có ít nhất 8 ký tự")
        }

        if (!password.any { it.isUpperCase() }) {
            return ValidationResult(false, "Mật khẩu phải có ít nhất 1 chữ hoa")
        }

        if (!password.any { it.isLowerCase() }) {
            return ValidationResult(false, "Mật khẩu phải có ít nhất 1 chữ thường")
        }

        if (!password.any { it.isDigit() }) {
            return ValidationResult(false, "Mật khẩu phải có ít nhất 1 số")
        }

        return ValidationResult(true, "Mật khẩu hợp lệ")
    }

    fun getRequirements(): String {
        return "Mật khẩu phải có:\n• Ít nhất 8 ký tự\n• 1 chữ hoa\n• 1 chữ thường\n• 1 số"
    }
}
