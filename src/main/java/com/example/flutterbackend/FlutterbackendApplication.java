package com.example.flutterbackend;

import com.example.flutterbackend.model.Category;
import com.example.flutterbackend.model.Transaction;
import com.example.flutterbackend.model.User;
import com.example.flutterbackend.repository.CategoryRepository;
import com.example.flutterbackend.repository.TransactionRepository;
import com.example.flutterbackend.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

@SpringBootApplication
@EnableScheduling // Enable scheduling for token cleanup
public class FlutterbackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlutterbackendApplication.class, args);
    }

    // Menambahkan data awal User dengan password yang di-hash
    @Bean
    public CommandLineRunner demoDataUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                System.out.println("=== Creating demo users ===");
                
                // Update existing users with hashed passwords
                userRepository.findAll().forEach(user -> {
                    try {
                        // Check if password is already hashed (BCrypt hashes start with $2a$, $2b$, or $2y$)
                        String currentPassword = user.getUserPassword();
                        if (currentPassword != null && !currentPassword.startsWith("$2")) {
                            System.out.println("Updating password for user: " + user.getUserEmail());
                            user.setUserPassword(passwordEncoder.encode(currentPassword));
                            userRepository.save(user);
                            System.out.println("Password updated for user: " + user.getUserEmail());
                        } else {
                            System.out.println("Password already hashed for user: " + user.getUserEmail());
                        }
                    } catch (Exception e) {
                        System.err.println("Error updating password for user " + user.getUserEmail() + ": " + e.getMessage());
                    }
                });
                
                // Create default user if none exists
                if (userRepository.count() == 0) {
                    User user = new User();
                    user.setUserName("User Default");
                    user.setUserEmail("user@example.com");
                    user.setUserPassword(passwordEncoder.encode("password123")); // Hash password
                    user.setUserProfile("default_profile");
                    user.setUserTransactions(new ArrayList<>());
                    user.setIsDeleted(false);
                    userRepository.save(user);
                    
                    System.out.println("User default berhasil dibuat dengan password ter-hash");
                } else {
                    System.out.println("Demo users setup completed");
                }
            } catch (Exception e) {
                System.err.println("Error creating demo user: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    // Menambahkan data awal Category
    @Bean
    public CommandLineRunner demoDataCategory(
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        return args -> {
            try {
                if (categoryRepository.count() == 0) {
                    // Ambil user default
                    User defaultUser = userRepository.findAll().stream().findFirst().orElse(null);
                    
                    if (defaultUser != null) {
                        // Buat kategori default
                        Category makanan = new Category("Makanan", true, defaultUser); // expense
                        Category gaji = new Category("Gaji", false, defaultUser); // income
                        Category transportasi = new Category("Transportasi", true, defaultUser); // expense
                        Category bonus = new Category("Bonus", false, defaultUser); // income
                        
                        categoryRepository.save(makanan);
                        categoryRepository.save(gaji);
                        categoryRepository.save(transportasi);
                        categoryRepository.save(bonus);
                        
                        System.out.println("Kategori default berhasil dibuat");
                    } else {
                        System.out.println("User belum tersedia untuk membuat kategori");
                    }
                } else {
                    System.out.println("Data kategori sudah ada");
                }
            } catch (Exception e) {
                System.err.println("Error creating demo categories: " + e.getMessage());
            }
        };
    }

    // Menambahkan data awal Transaction
    @Bean
    public CommandLineRunner demoDataTransaction(
            TransactionRepository transactionRepository, 
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        return args -> {
            try {
                if (transactionRepository.count() == 0) {
                    // Ambil user default
                    User defaultUser = userRepository.findAll().stream().findFirst().orElse(null);
                    
                    if (defaultUser != null) {
                        // Ambil kategori berdasarkan nama dan user
                        Category makanan = categoryRepository.findByCategoryNameAndCreatedBy("Makanan", defaultUser);
                        Category gaji = categoryRepository.findByCategoryNameAndCreatedBy("Gaji", defaultUser);
                        Category transportasi = categoryRepository.findByCategoryNameAndCreatedBy("Transportasi", defaultUser);

                        if (makanan != null && gaji != null && transportasi != null) {
                            // Buat transaksi dengan user
                            Transaction t1 = new Transaction(new BigDecimal("50000"), "Makan siang", LocalDate.now(), makanan, defaultUser);
                            Transaction t2 = new Transaction(new BigDecimal("5000000"), "Gaji Bulanan", LocalDate.now(), gaji, defaultUser);
                            Transaction t3 = new Transaction(new BigDecimal("200000"), "Bensin", LocalDate.now(), transportasi, defaultUser);
                            
                            transactionRepository.save(t1);
                            transactionRepository.save(t2);
                            transactionRepository.save(t3);
                            
                            System.out.println("Data transaksi berhasil dibuat");
                        } else {
                            System.out.println("Kategori belum tersedia untuk membuat transaksi");
                        }
                    } else {
                        System.out.println("User belum tersedia untuk membuat transaksi");
                    }
                } else {
                    System.out.println("Data transaksi sudah ada");
                }
            } catch (Exception e) {
                System.err.println("Error creating demo transactions: " + e.getMessage());
            }
        };
    }
}
