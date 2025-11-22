package com.example.Backend.service;

import com.example.Backend.entity.Brand;
import com.example.Backend.repository.BrandRepository;
import com.example.Backend.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public List<Brand> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        // Populate product count for each brand
        brands.forEach(brand -> {
            long count = productRepository.countByBrandId(brand.getId());
            brand.setProductCount((int) count);
        });
        return brands;
    }

    public Brand getBrandById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with id: " + id));
    }

    @Transactional
    public Brand createBrand(Brand brand) {
        if (brandRepository.existsByName(brand.getName())) {
            throw new IllegalArgumentException("Brand with name " + brand.getName() + " already exists");
        }
        return brandRepository.save(brand);
    }

    @Transactional
    public Brand updateBrand(Long id, Brand brandDetails) {
        Brand brand = getBrandById(id);

        if (!brand.getName().equals(brandDetails.getName()) &&
                brandRepository.existsByName(brandDetails.getName())) {
            throw new IllegalArgumentException("Brand with name " + brandDetails.getName() + " already exists");
        }

        brand.setName(brandDetails.getName());
        brand.setDescription(brandDetails.getDescription());
        brand.setCountry(brandDetails.getCountry());
        brand.setLogoUrl(brandDetails.getLogoUrl());

        return brandRepository.save(brand);
    }

    @Transactional
    public void deleteBrand(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new EntityNotFoundException("Brand not found with id: " + id);
        }
        brandRepository.deleteById(id);
    }
}
