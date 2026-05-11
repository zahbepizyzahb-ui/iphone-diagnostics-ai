package com.diagnostics.model

data class ComponentInfo(
    val id: String,
    val name: String,
    val type: String,
    val partNumber: String,
    val compatibleModels: List<String>,
    val description: String,
    val commonFaults: List<String>,
    val replacementDifficulty: Difficulty,
    val averagePrice: Double,
    val alternatives: List<String>,
    val datasheetUrl: String,
    val imageUrl: String
) {
    enum class Difficulty {
        EASY, MEDIUM, HARD, EXPERT
    }
}

// بيانات افتراضية لقاعدة البيانات
object DefaultComponents {
    val components = listOf(
        ComponentInfo(
            id = "1",
            name = "A16 Bionic SoC",
            type = "CPU",
            partNumber = "APL1W10",
            compatibleModels = listOf("iPhone 14 Pro", "iPhone 14 Pro Max"),
            description = "معالج Apple A16 Bionic بتقنية 4nm، يحتوي على 6 أنوية CPU و5 أنوية GPU",
            commonFaults = listOf(
                "تسخين مفرط",
                "تعليق الجهاز",
                "إعادة تشغيل تلقائي",
                "بطء في الأداء"
            ),
            replacementDifficulty = ComponentInfo.Difficulty.EXPERT,
            averagePrice = 150.0,
            alternatives = listOf("APL1W09 (A15)", "APL1W11 (A16 معدل)"),
            datasheetUrl = "https://www.apple.com/a16-bionic",
            imageUrl = ""
        ),
        ComponentInfo(
            id = "2",
            name = "Power Management IC",
            type = "POWER_IC",
            partNumber = "338S00817",
            compatibleModels = listOf("iPhone 14", "iPhone 14 Plus", "iPhone 14 Pro", "iPhone 14 Pro Max"),
            description = "دائرة إدارة الطاقة الرئيسية، تتحكم في الشحن والتفريغ وتوزيع الطاقة",
            commonFaults = listOf(
                "عدم الشحن",
                "استهلاك سريع للبطارية",
                "تسخين أثناء الشحن",
                "إعادة تشغيل متكررة"
            ),
            replacementDifficulty = ComponentInfo.Difficulty.HARD,
            averagePrice = 25.0,
            alternatives = listOf("338S00816", "338S00818"),
            datasheetUrl = "",
            imageUrl = ""
        ),
        ComponentInfo(
            id = "3",
            name = "Charging IC",
            type = "CHARGING_IC",
            partNumber = "SN2012010",
            compatibleModels = listOf("iPhone 14", "iPhone 14 Plus"),
            description = "دائرة الشحن السريع، تدعم USB-C PD حتى 20W",
            commonFaults = listOf(
                "عدم الشحن السريع",
                "رسالة 'غير مدعوم'",
                "شحن بطيء",
                "تسخين أثناء الشحن"
            ),
            replacementDifficulty = ComponentInfo.Difficulty.MEDIUM,
            averagePrice = 15.0,
            alternatives = listOf("1610A3", "610A3B"),
            datasheetUrl = "",
            imageUrl = ""
        ),
        ComponentInfo(
            id = "4",
            name = "Wi-Fi / Bluetooth Module",
            type = "WIFI_MODULE",
            partNumber = "339S00761",
            compatibleModels = listOf("iPhone 14 Pro", "iPhone 14 Pro Max"),
            description = "وحدة الواي فاي 6E والبلوتوث 5.3",
            commonFaults = listOf(
                "ضعف إشارة الواي فاي",
                "قطع الاتصال المتكرر",
                "عدم اكتشاف الشبكات",
                "مشاكل البلوتوث"
            ),
            replacementDifficulty = ComponentInfo.Difficulty.HARD,
            averagePrice = 30.0,
            alternatives = listOf("339S00760", "BCM4389"),
            datasheetUrl = "",
            imageUrl = ""
        ),
        ComponentInfo(
            id = "5",
            name = "Audio Codec IC",
            type = "AUDIO_IC",
            partNumber = "338S00105",
            compatibleModels = listOf("iPhone 13", "iPhone 13 Pro", "iPhone 14"),
            description = "دائرة معالجة الصوت الرئيسية",
            commonFaults = listOf(
                "عدم وجود صوت",
                "صوت مشوش",
                "ميكروفون لا يعمل",
                "تأخر في الصوت"
            ),
            replacementDifficulty = ComponentInfo.Difficulty.HARD,
            averagePrice = 20.0,
            alternatives = listOf("338S00104", "CS35L26"),
            datasheetUrl = "",
            imageUrl = ""
        ),
        ComponentInfo(
            id = "6",
            name = "Li-ion Battery",
            type = "BATTERY",
            partNumber = "616-00675",
            compatibleModels = listOf("iPhone 14 Pro Max"),
            description = "بطارية ليثيوم أيون 4323mAh",
            commonFaults = listOf(
                "تضخم البطارية",
                "استهلاك سريع",
                "إغلاق مفاجئ عند 20-30%",
                "عدم الشحن الكامل"
            ),
            replacementDifficulty = ComponentInfo.Difficulty.EASY,
            averagePrice = 45.0,
            alternatives = listOf("616-00674", "بطارية OEM"),
            datasheetUrl = "",
            imageUrl = ""
        ),
        ComponentInfo(
            id = "7",
            name = "Taptic Engine",
            type = "SENSOR",
            partNumber = "TAPTIC-14PM",
            compatibleModels = listOf("iPhone 14 Pro Max"),
            description = "محرك الاهتزاز الخطي",
            commonFaults = listOf(
                "عدم الاهتزاز",
                "اهتزاز ضعيف",
                "صوت غريب"
            ),
            replacementDifficulty = ComponentInfo.Difficulty.EASY,
            averagePrice = 35.0,
            alternatives = listOf("TAPTIC-14P"),
            datasheetUrl = "",
            imageUrl = ""
        ),
        ComponentInfo(
            id = "8",
            name = "Face ID Sensor",
            type = "SENSOR",
            partNumber = "FACEID-14P",
            compatibleModels = listOf("iPhone 14 Pro"),
            description = "مجموعة مستشعرات Face ID (infrared + dot projector)",
            commonFaults = listOf(
                "Face ID لا يعمل",
                "رسالة 'أبعد iPhone'",
                "عدم اكتشاف الوجه",
                "خطأ في العمق"
            ),
            replacementDifficulty = ComponentInfo.Difficulty.EXPERT,
            averagePrice = 80.0,
            alternatives = listOf("لا يوجد - مبرمج على الجهاز"),
            datasheetUrl = "",
            imageUrl = ""
        )
    )
}
