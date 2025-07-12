package ma.enset.ziyara.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.ziyara.city.entity.City;
import ma.enset.ziyara.city.repository.CityRepository;
import ma.enset.ziyara.destination.entity.Destination;
import ma.enset.ziyara.destination.entity.DestinationImage;
import ma.enset.ziyara.destination.entity.DestinationTag;
import ma.enset.ziyara.destination.entity.DestinationType;
import ma.enset.ziyara.destination.repository.DestinationRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSeedingService implements ApplicationRunner {

    private final CityRepository cityRepository;
    private final DestinationRepository destinationRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (shouldSeedData()) {
            log.info("Starting data seeding...");
            seedCities();
            seedDestinations();
            log.info("Data seeding completed!");
        } else {
            log.info("Data already exists, skipping seeding.");
        }
    }

    private boolean shouldSeedData() {
        return cityRepository.count() == 0 && destinationRepository.count() == 0;
    }

    private void seedCities() {
        log.info("Seeding cities...");

        List<City> cities = List.of(
                createCity("Casablanca", "الدار البيضاء", "Casablanca-Settat",
                        33.5731, -7.5898, "Economic capital of Morocco", true),
                createCity("Marrakech", "مراكش", "Marrakech-Safi",
                        31.6295, -7.9811, "The Red City - Famous for its historic medina", true),
                createCity("Fès", "فاس", "Fès-Meknès",
                        34.0181, -5.0078, "Cultural capital with the world's oldest university", true),
                createCity("Rabat", "الرباط", "Rabat-Salé-Kénitra",
                        34.0209, -6.8416, "Political capital and royal city", true),
                createCity("Tangier", "طنجة", "Tanger-Tétouan-Al Hoceïma",
                        35.7595, -5.8340, "Gateway between Africa and Europe", true),
                createCity("Agadir", "أكادير", "Souss-Massa",
                        30.4278, -9.5981, "Popular beach resort destination", true),
                createCity("Meknes", "مكناس", "Fès-Meknès",
                        33.8935, -5.5473, "Imperial city with beautiful architecture", false),
                createCity("Oujda", "وجدة", "Oriental",
                        34.6814, -1.9086, "Eastern gateway with nearby oases", false),
                createCity("Tetouan", "تطوان", "Tanger-Tétouan-Al Hoceïma",
                        35.5889, -5.3626, "Andalusian influenced white city", false),
                createCity("Essaouira", "الصويرة", "Marrakech-Safi",
                        31.5125, -9.7749, "Coastal city famous for windsurfing", true),
                createCity("Ouarzazate", "ورزازات", "Drâa-Tafilalet",
                        30.9335, -6.9370, "Gateway to the Sahara Desert", false),
                createCity("Chefchaouen", "شفشاون", "Tanger-Tétouan-Al Hoceïma",
                        35.1681, -5.2636, "The Blue Pearl of Morocco", true)
        );

        cityRepository.saveAll(cities);
        log.info("Seeded {} cities", cities.size());
    }

    private void seedDestinations() {
        log.info("Seeding destinations...");

        // Get cities for reference
        List<City> cities = cityRepository.findAll();
        City casablanca = findCityByName(cities, "Casablanca");
        City marrakech = findCityByName(cities, "Marrakech");
        City fes = findCityByName(cities, "Fès");
        City rabat = findCityByName(cities, "Rabat");
        City essaouira = findCityByName(cities, "Essaouira");
        City chefchaouen = findCityByName(cities, "Chefchaouen");
        City agadir = findCityByName(cities, "Agadir");

        List<Destination> destinations = List.of(
                // Casablanca Destinations
                createDestination("Hassan II Mosque", "One of the largest mosques in the world with stunning oceanfront location",
                        DestinationType.RELIGIOUS, casablanca, BigDecimal.valueOf(120),
                        33.6084, -7.6325, Set.of("mosque", "religious", "architecture", "ocean"),
                        Set.of("https://example.com/hassan-ii-1.jpg", "https://example.com/hassan-ii-2.jpg")),

                createDestination("Old Medina of Casablanca", "Historic quarter with traditional souks and authentic Moroccan atmosphere",
                        DestinationType.HISTORICAL, casablanca, BigDecimal.ZERO,
                        33.5950, -7.6187, Set.of("medina", "historical", "shopping", "traditional"),
                        Set.of("https://example.com/medina-casa-1.jpg")),

                createDestination("Morocco Mall", "Largest shopping center in Africa with modern amenities",
                        DestinationType.SHOPPING, casablanca, BigDecimal.ZERO,
                        33.5426, -7.6792, Set.of("shopping", "modern", "family", "entertainment"),
                        Set.of("https://example.com/morocco-mall-1.jpg")),

                // Marrakech Destinations
                createDestination("Jemaa el-Fnaa", "Famous main square and marketplace - heart of Marrakech",
                        DestinationType.CULTURAL, marrakech, BigDecimal.ZERO,
                        31.6258, -7.9890, Set.of("square", "cultural", "entertainment", "food"),
                        Set.of("https://example.com/jemaa-1.jpg", "https://example.com/jemaa-2.jpg")),

                createDestination("Bahia Palace", "Beautiful 19th century palace with stunning architecture",
                        DestinationType.HISTORICAL, marrakech, BigDecimal.valueOf(70),
                        31.6214, -7.9844, Set.of("palace", "architecture", "historical", "garden"),
                        Set.of("https://example.com/bahia-1.jpg")),

                createDestination("Majorelle Garden", "Exotic garden with vibrant blue villa and cactus collection",
                        DestinationType.NATURE, marrakech, BigDecimal.valueOf(150),
                        31.6307, -8.0034, Set.of("garden", "nature", "photography", "art"),
                        Set.of("https://example.com/majorelle-1.jpg", "https://example.com/majorelle-2.jpg")),

                // Fès Destinations
                createDestination("Fès el-Bali", "UNESCO World Heritage medina - largest car-free urban area",
                        DestinationType.HISTORICAL, fes, BigDecimal.ZERO,
                        34.0608, -4.9972, Set.of("medina", "unesco", "historical", "maze"),
                        Set.of("https://example.com/fes-medina-1.jpg")),

                createDestination("University of Al Qarawiyyin", "World's oldest continuously operating university",
                        DestinationType.EDUCATIONAL, fes, BigDecimal.valueOf(50),
                        34.0642, -4.9978, Set.of("university", "historical", "education", "library"),
                        Set.of("https://example.com/qarawiyyin-1.jpg")),

                // Rabat Destinations
                createDestination("Kasbah of the Udayas", "Historic fortress overlooking the Atlantic Ocean",
                        DestinationType.HISTORICAL, rabat, BigDecimal.valueOf(20),
                        34.0375, -6.8359, Set.of("kasbah", "historical", "ocean", "fortress"),
                        Set.of("https://example.com/udayas-1.jpg")),

                createDestination("Hassan Tower", "Minaret of an incomplete 12th century mosque",
                        DestinationType.HISTORICAL, rabat, BigDecimal.ZERO,
                        34.0244, -6.8214, Set.of("tower", "historical", "architecture", "landmark"),
                        Set.of("https://example.com/hassan-tower-1.jpg")),

                // Essaouira Destinations
                createDestination("Essaouira Beach", "Beautiful Atlantic coast beach perfect for water sports",
                        DestinationType.BEACH, essaouira, BigDecimal.ZERO,
                        31.5084, -9.7595, Set.of("beach", "ocean", "windsurfing", "relaxation"),
                        Set.of("https://example.com/essaouira-beach-1.jpg")),

                createDestination("Essaouira Medina", "UNESCO World Heritage coastal medina with Portuguese influence",
                        DestinationType.HISTORICAL, essaouira, BigDecimal.ZERO,
                        31.5125, -9.7699, Set.of("medina", "unesco", "coastal", "artisan"),
                        Set.of("https://example.com/essaouira-medina-1.jpg")),

                // Chefchaouen Destinations
                createDestination("Blue Pearl Medina", "Famous blue-painted streets and buildings",
                        DestinationType.CULTURAL, chefchaouen, BigDecimal.ZERO,
                        35.1681, -5.2636, Set.of("blue", "photography", "medina", "mountain"),
                        Set.of("https://example.com/chefchaouen-blue-1.jpg")),

                // Agadir Destinations
                createDestination("Agadir Beach", "Modern beach resort with golden sand and surfing",
                        DestinationType.BEACH, agadir, BigDecimal.ZERO,
                        30.4202, -9.5982, Set.of("beach", "resort", "surfing", "modern"),
                        Set.of("https://example.com/agadir-beach-1.jpg"))
        );

        destinationRepository.saveAll(destinations);
        log.info("Seeded {} destinations", destinations.size());
    }

    private City createCity(String name, String arabicName, String region,
                            Double latitude, Double longitude, String description, Boolean isPopular) {
        return City.builder()
                .name(name)
                .arabicName(arabicName)
                .region(region)
                .latitude(latitude)
                .longitude(longitude)
                .description(description)
                .isPopular(isPopular)
                .build();
    }

    private Destination createDestination(String name, String description, DestinationType type,
                                          City city, BigDecimal price, Double latitude, Double longitude,
                                          Set<String> tagNames, Set<String> imageUrls) {

        Destination destination = Destination.builder()
                .name(name)
                .description(description)
                .type(type)
                .city(city)
                .price(price)
                .latitude(latitude)
                .longitude(longitude)
                .active(true)
                .averageRating(4.0 + Math.random()) // Random rating between 4.0 and 5.0
                .reviewCount((long) (Math.random() * 100 + 10)) // Random review count
                .build();

        // Create tags
        Set<DestinationTag> tags = tagNames.stream()
                .map(tagName -> DestinationTag.builder()
                        .name(tagName)
                        .destination(destination)
                        .build())
                .collect(java.util.stream.Collectors.toSet());

        // Create images
        int order = 0;
        Set<DestinationImage> images = imageUrls.stream()
                .map(url -> DestinationImage.builder()
                        .imageUrl(url)
                        .displayOrder(order)
                        .destination(destination)
                        .build())
                .collect(java.util.stream.Collectors.toSet());

        destination.setTags(tags);
        destination.setImages(images);

        return destination;
    }

    private City findCityByName(List<City> cities, String name) {
        return cities.stream()
                .filter(city -> city.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("City not found: " + name));
    }
}