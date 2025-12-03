package com.integmobile.backend.config

import com.integmobile.backend.model.entity.Product
import com.integmobile.backend.repository.ProductRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataSeeder {
    
    @Bean
    fun seedData(productRepository: ProductRepository): CommandLineRunner {
        return CommandLineRunner {
            if (productRepository.count() == 0) {
                val products = listOf(
                    Product(
                        name = "Brake Pads - Premium",
                        description = "High-performance ceramic brake pads for superior stopping power",
                        price = 4500.0,
                        originalPrice = 5500.0,
                        discount = 18,
                        brand = "Bosch",
                        category = "Brakes",
                        imageUrls = arrayOf("https://via.placeholder.com/400"),
                        inStock = true,
                        quantityAvailable = 25,
                        specifications = mapOf(
                            "Material" to "Ceramic",
                            "Warranty" to "2 years",
                            "Weight" to "1.2kg"
                        ),
                        compatibility = arrayOf("Toyota Corolla", "Honda Civic", "Nissan Sentra")
                    ),
                    Product(
                        name = "Engine Oil Filter",
                        description = "OEM quality oil filter for optimal engine protection",
                        price = 850.0,
                        brand = "Mann Filter",
                        category = "Filters",
                        imageUrls = arrayOf("https://via.placeholder.com/400"),
                        inStock = true,
                        quantityAvailable = 50,
                        specifications = mapOf(
                            "Type" to "Spin-on",
                            "Thread Size" to "3/4-16",
                            "Height" to "95mm"
                        ),
                        compatibility = arrayOf("Toyota Camry", "Honda Accord", "Mazda 6")
                    ),
                    Product(
                        name = "Air Filter - High Flow",
                        description = "Performance air filter for improved airflow and engine efficiency",
                        price = 1200.0,
                        originalPrice = 1500.0,
                        discount = 20,
                        brand = "K&N",
                        category = "Filters",
                        imageUrls = arrayOf("https://via.placeholder.com/400"),
                        inStock = true,
                        quantityAvailable = 30,
                        specifications = mapOf(
                            "Material" to "Cotton Gauze",
                            "Reusable" to "Yes",
                            "Warranty" to "10 years"
                        ),
                        compatibility = arrayOf("Ford Focus", "Volkswagen Golf", "Audi A3")
                    ),
                    Product(
                        name = "Spark Plugs Set (4pcs)",
                        description = "Iridium spark plugs for better fuel economy and performance",
                        price = 3200.0,
                        brand = "NGK",
                        category = "Ignition",
                        imageUrls = arrayOf("https://via.placeholder.com/400"),
                        inStock = true,
                        quantityAvailable = 40,
                        specifications = mapOf(
                            "Material" to "Iridium",
                            "Gap" to "1.1mm",
                            "Quantity" to "4 pieces"
                        ),
                        compatibility = arrayOf("Honda Civic", "Toyota Corolla", "Mazda 3")
                    ),
                    Product(
                        name = "Battery - 12V 70Ah",
                        description = "Maintenance-free car battery with 3-year warranty",
                        price = 8500.0,
                        brand = "Varta",
                        category = "Electrical",
                        imageUrls = arrayOf("https://via.placeholder.com/400"),
                        inStock = true,
                        quantityAvailable = 15,
                        specifications = mapOf(
                            "Voltage" to "12V",
                            "Capacity" to "70Ah",
                            "CCA" to "680A",
                            "Warranty" to "3 years"
                        ),
                        compatibility = arrayOf("Most sedans and hatchbacks")
                    ),
                    Product(
                        name = "Windshield Wipers (Pair)",
                        description = "All-season windshield wipers for clear visibility",
                        price = 1800.0,
                        brand = "Bosch",
                        category = "Accessories",
                        imageUrls = arrayOf("https://via.placeholder.com/400"),
                        inStock = true,
                        quantityAvailable = 60,
                        specifications = mapOf(
                            "Length" to "24/18 inches",
                            "Type" to "Beam blade",
                            "Warranty" to "1 year"
                        ),
                        compatibility = arrayOf("Universal fit")
                    ),
                    Product(
                        name = "Headlight Bulbs H7 (Pair)",
                        description = "Bright white halogen headlight bulbs",
                        price = 950.0,
                        brand = "Philips",
                        category = "Lighting",
                        imageUrls = arrayOf("https://via.placeholder.com/400"),
                        inStock = true,
                        quantityAvailable = 45,
                        specifications = mapOf(
                            "Type" to "H7",
                            "Wattage" to "55W",
                            "Color Temperature" to "4300K"
                        ),
                        compatibility = arrayOf("Most European cars")
                    ),
                    Product(
                        name = "Cabin Air Filter",
                        description = "Activated carbon cabin filter for clean air",
                        price = 650.0,
                        brand = "Mann Filter",
                        category = "Filters",
                        imageUrls = arrayOf("https://via.placeholder.com/400"),
                        inStock = false,
                        quantityAvailable = 0,
                        specifications = mapOf(
                            "Type" to "Activated Carbon",
                            "Filtration" to "99.5%"
                        ),
                        compatibility = arrayOf("Toyota Camry", "Honda Accord")
                    )
                )
                
                productRepository.saveAll(products)
                println("✅ Seeded ${products.size} products")
            }
        }
    }
}
