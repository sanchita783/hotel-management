package com.example.config;

import com.example.entity.Role;
import com.example.entity.Room;
import com.example.entity.RoomStatus;
import com.example.entity.RoomType;
import com.example.entity.User;
import com.example.repository.RoomRepository;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@grandhotel.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@1234}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        createDefaultAdmin();
        seedRooms();
    }

    private void createDefaultAdmin() {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists: {}", adminEmail);
            return;
        }

        User admin = User.builder()
                .firstName("Hotel")
                .lastName("Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .phone("9000000000")
                .role(Role.ADMIN)
                .isActive(true)
                .emailVerified(true)
                .build();

        userRepository.save(admin);
        log.info("✅ Default admin created: {} / {}", adminEmail, adminPassword);
        log.warn("⚠️  Change the default admin password immediately in production!");
    }

    private void seedRooms() {
        if (roomRepository.count() > 0) {
            log.info("Rooms already seeded, skipping.");
            return;
        }

        List<Room> rooms = Arrays.asList(
            Room.builder()
                .roomNumber("101")
                .roomType(RoomType.SINGLE)
                .pricePerNight(new BigDecimal("1200"))
                .capacity(1)
                .description("Perfect for solo travellers. Minimalist design with city views and premium bedding.")
                .floorNumber(1)
                .roomSize(200.0)
                .amenities(Arrays.asList("Free WiFi", "AC", "Smart TV", "Mini Fridge"))
                .images(Arrays.asList("https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=600&q=80"))
                .roomStatus(RoomStatus.AVAILABLE)
                .isActive(true)
                .build(),

            Room.builder()
                .roomNumber("103")
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("2500"))
                .capacity(2)
                .description("Spacious king-bed room with panoramic views and luxury bathroom amenities.")
                .floorNumber(1)
                .roomSize(320.0)
                .amenities(Arrays.asList("Free WiFi", "AC", "Smart TV", "Mini Bar", "Safe"))
                .images(Arrays.asList("https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=600&q=80"))
                .roomStatus(RoomStatus.AVAILABLE)
                .isActive(true)
                .build(),

            Room.builder()
                .roomNumber("201")
                .roomType(RoomType.TWIN)
                .pricePerNight(new BigDecimal("2800"))
                .capacity(2)
                .description("Two premium single beds, ideal for friends or colleagues travelling together.")
                .floorNumber(2)
                .roomSize(300.0)
                .amenities(Arrays.asList("Free WiFi", "AC", "Smart TV", "Work Desk", "Pool Access"))
                .images(Arrays.asList("https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=600&q=80"))
                .roomStatus(RoomStatus.AVAILABLE)
                .isActive(true)
                .build(),

            Room.builder()
                .roomNumber("202")
                .roomType(RoomType.DELUXE)
                .pricePerNight(new BigDecimal("3800"))
                .capacity(2)
                .description("Stunning sea view with a lavish bathtub, private lounge, and premium bar.")
                .floorNumber(2)
                .roomSize(450.0)
                .amenities(Arrays.asList("Free WiFi", "AC", "55\" TV", "Jacuzzi", "Mini Bar", "Balcony"))
                .images(Arrays.asList("https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=600&q=80"))
                .roomStatus(RoomStatus.AVAILABLE)
                .isActive(true)
                .build(),

            Room.builder()
                .roomNumber("203")
                .roomType(RoomType.FAMILY)
                .pricePerNight(new BigDecimal("4500"))
                .capacity(4)
                .description("Two bedrooms, a living area and kitchenette — perfect for families.")
                .floorNumber(2)
                .roomSize(600.0)
                .amenities(Arrays.asList("Free WiFi", "AC", "2 Smart TVs", "Kitchenette", "Safe"))
                .images(Arrays.asList("https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=600&q=80"))
                .roomStatus(RoomStatus.AVAILABLE)
                .isActive(true)
                .build(),

            Room.builder()
                .roomNumber("301")
                .roomType(RoomType.SUITE)
                .pricePerNight(new BigDecimal("7800"))
                .capacity(2)
                .description("Private balcony, Jacuzzi, butler service and breathtaking city panorama.")
                .floorNumber(3)
                .roomSize(800.0)
                .amenities(Arrays.asList("Free WiFi", "AC", "65\" TV", "Jacuzzi", "Butler", "Balcony", "Espresso"))
                .images(Arrays.asList("https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=600&q=80"))
                .roomStatus(RoomStatus.AVAILABLE)
                .isActive(true)
                .build(),

            Room.builder()
                .roomNumber("302")
                .roomType(RoomType.STUDIO)
                .pricePerNight(new BigDecimal("3200"))
                .capacity(2)
                .description("Modern studio with kitchenette and workspace, ideal for extended stays.")
                .floorNumber(3)
                .roomSize(380.0)
                .amenities(Arrays.asList("Free WiFi", "AC", "Smart TV", "Kitchenette", "Work Desk"))
                .images(Arrays.asList("https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=600&q=80"))
                .roomStatus(RoomStatus.AVAILABLE)
                .isActive(true)
                .build(),

            Room.builder()
                .roomNumber("401")
                .roomType(RoomType.PRESIDENTIAL)
                .pricePerNight(new BigDecimal("25000"))
                .capacity(4)
                .description("The pinnacle of luxury — private pool, personal butler, panoramic 360° views.")
                .floorNumber(4)
                .roomSize(2000.0)
                .amenities(Arrays.asList("Free WiFi", "AC", "75\" TV", "Private Pool", "Butler", "Bar", "Piano"))
                .images(Arrays.asList("https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=600&q=80"))
                .roomStatus(RoomStatus.AVAILABLE)
                .isActive(true)
                .build()
        );

        roomRepository.saveAll(rooms);
        log.info("✅ {} rooms seeded successfully!", rooms.size());
    }
}
