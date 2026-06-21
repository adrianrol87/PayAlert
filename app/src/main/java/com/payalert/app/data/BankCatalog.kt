package com.payalert.app.data

enum class BankCategory(val label: String) {
    Banco("Bancos"),
    Departamental("Departamentales"),
    Fintech("Fintech"),
}

data class Bank(
    val id: String,
    val name: String,
    val category: BankCategory,
    val cardTypes: List<String>,
)

object BankCatalog {
    val banks = listOf(
        Bank("bbva", "BBVA", BankCategory.Banco, listOf("Azul", "Oro", "Platinum", "Vive", "Start", "Crea", "Primera", "Educacion", "IPN", "UNAM", "Rayados")),
        Bank("banamex", "Banamex", BankCategory.Banco, listOf("Clasica", "Oro", "Platinum", "Lineup", "Costco", "Home Depot", "Teleton", "Joy", "Affinity", "Beyond", "Comer", "Conquista", "Descubre", "Explora")),
        Bank("santander", "Santander", BankCategory.Banco, listOf("LikeU", "Gold", "Platinum", "World Elite", "Amex", "Fiesta Oro", "Fiesta Platino", "Aeromexico BCA", "Aeromexico Platino", "Aeromexico Infinite")),
        Bank("banorte", "Banorte", BankCategory.Banco, listOf("Clasica", "Oro", "Seleccion Nacional", "One Up", "United Businness", "La Comer", "Platinum", "AT&T Elite", "AT&T", "Por ti", "Ke Buena", "Los 40", "Mujer", "W Radio")),
        Bank("hsbc", "HSBC", BankCategory.Banco, listOf("Viva Plus", "Viva", "Premier", "Premier World Elite", "Clasica", "Air", "Advance Platinum", "2Now", "Zero")),
        Bank("scotiabank", "Scotiabank", BankCategory.Banco, listOf("Scotia Travel", "Viva", "Ideal", "Signature", "Travel World Elite", "Travel Platinum", "Travel Oro", "Basica")),
        Bank("banregio", "Banregio", BankCategory.Banco, listOf("Oro", "Platino", "Clasica", "Mas")),
        Bank("azteca", "Banco Azteca", BankCategory.Banco, listOf("Clasica", "Oro")),
        Bank("afirme", "Afirme", BankCategory.Banco, listOf("Oro", "Construrama", "HEB", "Clasica", "Platinum", "Tigres", "Basica", "Blanc")),
        Bank("invex", "Invex", BankCategory.Banco, listOf("despegargold", "despegarplat", "ikea", "sams", "volaris", "volaris0", "volaris2", "voyage", "voyageplat", "walmart")),
        Bank("inbursa", "Inbursa", BankCategory.Banco, listOf("Clasica", "Oro", "Platinum", "Bodega Aurrera", "Naturgy", "Enlace Medico", "Walmart", "Sams", "Telcel", "Leon")),
        Bank("banbajio", "BanBajio", BankCategory.Banco, listOf("Clasica", "Oro", "Platinum")),
        Bank("liverpool", "Liverpool", BankCategory.Departamental, listOf("Visa", "Clasica", "Garantizada")),
        Bank("palacio", "Palacio de Hierro", BankCategory.Departamental, listOf("Clasica", "Socio", "Total")),
        Bank("sears", "Sears", BankCategory.Departamental, listOf("Gold", "Black")),
        Bank("suburbia", "Suburbia", BankCategory.Departamental, listOf("Clasica", "Total")),
        Bank("coppel", "Coppel", BankCategory.Departamental, listOf("Clasica", "Oro", "Platinum")),
        Bank("c&a", "C&A", BankCategory.Departamental, listOf("Trend", "Visa", "Pay")),
        Bank("famsa", "Famsa", BankCategory.Departamental, listOf("Clasica")),
        Bank("nu", "Nu", BankCategory.Fintech, listOf("Clasica")),
        Bank("hey", "Hey Banco", BankCategory.Fintech, listOf("Clasica")),
        Bank("rappi", "Rappi", BankCategory.Fintech, listOf("Clasica")),
        Bank("didi", "DiDi", BankCategory.Fintech, listOf("Clasica")),
        Bank("plata", "Plata", BankCategory.Fintech, listOf("Clasica")),
        Bank("mercadolibre", "Mercado Libre", BankCategory.Fintech, listOf("Clasica")),
        Bank("vexi", "Vexi", BankCategory.Fintech, listOf("Carnet", "American Express")),
        Bank("stori", "Stori", BankCategory.Fintech, listOf("Construye")),
        Bank("klar", "Klar", BankCategory.Fintech, listOf("Clasica")),
        Bank("aplazo", "Aplazo", BankCategory.Fintech, listOf("Clasica")),
    )

    val grouped = BankCategory.entries.map { category ->
        category to banks.filter { it.category == category }
    }
}
