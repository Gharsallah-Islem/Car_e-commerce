package com.example.Backend.util;

import com.example.Backend.entity.Brand;
import com.example.Backend.entity.Category;
import com.example.Backend.repository.BrandRepository;
import com.example.Backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataMigrationRunner implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Checking Categories and Brands ===");

        long categoryCount = categoryRepository.count();
        long brandCount = brandRepository.count();

        if (categoryCount > 0 && brandCount > 0) {
            log.info("Categories: {}, Brands: {} - Data already exists", categoryCount, brandCount);
            return;
        }

        log.info("Creating default categories and brands...");
        createDefaultData();
    }

    @Transactional
    private void createDefaultData() {
        // Create default categories
        if (categoryRepository.count() == 0) {
            String[] categoryNames = {
                    "Freinage", "Moteur", "Suspension", "Électrique",
                    "Carrosserie", "Filtration", "Transmission", "Éclairage"
            };

            for (String name : categoryNames) {
                Category category = Category.builder()
                        .name(name)
                        .description("Catégorie " + name)
                        .build();
                categoryRepository.save(category);
                log.info("Created category: {}", name);
            }
        }

        // Create default brands
        if (brandRepository.count() == 0) {
            String[][] brandData = {
                    { "Bosch", "Allemagne" },
                    { "Valeo", "France" },
                    { "Brembo", "Italie" },
                    { "NGK", "Japon" },
                    { "Mann-Filter", "Allemagne" },
                    { "Sachs", "Allemagne" },
                    { "Hella", "Allemagne" },
                    { "Continental", "Allemagne" }
            };

            for (String[] data : brandData) {
                Brand brand = Brand.builder()
                        .name(data[0])
                        .country(data[1])
                        .description("Marque " + data[0])
                        .build();
                brandRepository.save(brand);
                log.info("Created brand: {} ({})", data[0], data[1]);
            }
        }

        log.info("=== Default data creation completed ===");
    }
}
