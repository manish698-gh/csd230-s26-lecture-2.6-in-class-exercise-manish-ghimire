package csd230.s26.lab1;

import com.github.javafaker.Faker;
import csd230.s26.lab1.entities.*;
import csd230.s26.lab1.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;  // ADD THIS

import java.time.LocalDateTime;

@SpringBootApplication
public class Lab1Application implements CommandLineRunner {

	// Final fields for Constructor Injection
	private final BookRepository bookRepository;
	private final MagazineRepository magazineRepository;
	private final DiscMagRepository discMagRepository;
	private final TicketRepository ticketRepository;
	private final ProductRepository productRepository;
	private final CartRepository cartRepository;
	private final PasswordEncoder passwordEncoder;  // ADD THIS
	private final UserRepository userRepository;    // ADD THIS

	// Hardened Constructor Injection (Standard for S26)
	public Lab1Application(BookRepository bookRepository,
	                       MagazineRepository magazineRepository,
	                       DiscMagRepository discMagRepository,
	                       TicketRepository ticketRepository,
	                       ProductRepository productRepository,
	                       CartRepository cartRepository,
	                       PasswordEncoder passwordEncoder,  // ADD THIS
	                       UserRepository userRepository) {  // ADD THIS
		this.bookRepository = bookRepository;
		this.magazineRepository = magazineRepository;
		this.discMagRepository = discMagRepository;
		this.ticketRepository = ticketRepository;
		this.productRepository = productRepository;
		this.cartRepository = cartRepository;
		this.passwordEncoder = passwordEncoder;  // ADD THIS
		this.userRepository = userRepository;    // ADD THIS
	}

	public static void main(String[] args) {
		SpringApplication.run(Lab1Application.class, args);
	}

	/**
	 * The run method executes after the application context is loaded.
	 * @Transactional ensures the Hibernate Session remains open for the entire
	 * duration of the method, preventing LazyInitializationExceptions when
	 * accessing polymorphic collections or relationships.
	 */
	@Override
	@Transactional
	public void run(String... args) throws Exception {
		Faker faker = new Faker();

		// --- 1. Create Books ---
		System.out.println("Generating Books...");
		for (int i = 0; i < 3; i++) {
			BookEntity book = new BookEntity(
					faker.book().author(),
					faker.book().title(),
					Double.parseDouble(faker.commerce().price(10.0, 50.0)),
					faker.number().numberBetween(1, 100)
			);
			bookRepository.save(book);
		}

		// --- 2. Create Magazines ---
		System.out.println("Generating Magazines...");
		for (int i = 0; i < 3; i++) {
			MagazineEntity mag = new MagazineEntity(
					faker.number().numberBetween(10, 100),       // Order Qty
					LocalDateTime.now().minusDays(i),            // Current Issue Date
					faker.book().genre() + " Magazine",          // Title
					Double.parseDouble(faker.commerce().price(5.0, 20.0)), // Price
					faker.number().numberBetween(10, 500)        // Copies
			);
			magazineRepository.save(mag);
			System.out.println("Saved Magazine: " + mag.getTitle());
		}

		// --- 3. Create Tickets ---
		System.out.println("Generating Tickets...");
		for (int i = 0; i < 3; i++) {
			String eventName = faker.commerce().department() + " " + faker.company().suffix();
			TicketEntity ticket = new TicketEntity(
					eventName + " Ticket",
					Double.parseDouble(faker.commerce().price(5.0, 100.0))
			);
			ticketRepository.save(ticket);
			System.out.println("Saved Ticket: " + ticket.getDescription());
		}

		// --- 4. Create DiscMags ---
		System.out.println("Generating DiscMags...");
		for (int i = 0; i < 3; i++) {
			DiscMagEntity discMag = new DiscMagEntity(
					faker.bool().bool(),                         // Has Disc?
					faker.number().numberBetween(10, 100),       // Order Qty
					LocalDateTime.now().minusDays(i),            // Current Issue Date
					faker.book().title() + " (with Disc)",       // Title
					Double.parseDouble(faker.commerce().price(10.0, 30.0)), // Price
					faker.number().numberBetween(5, 50)          // Copies
			);
			discMagRepository.save(discMag);
			System.out.println("Saved DiscMag: " + discMag.getTitle());
		}

		System.out.println("\nDatabase initialization complete.");

		// --- 5. List All Products (Polymorphic Retrieval) ---
		System.out.println("\n--- Listing All Products from ProductRepository ---");
		productRepository.findAll().forEach(product -> {
			// Polymorphism in action: calling toString() on the base type
			// executes the specific implementation for the subclass.
			System.out.println(product.toString());
		});

		// 1. Create and save a Cart
		CartEntity cart = new CartEntity();
		cartRepository.save(cart);

		// 2. Retrieve an existing book from your generation loop
		// get the first book
		BookEntity someBook = bookRepository.findAll().get(0);  // ... retrieve one of your generated books ...

		// 3. Add to cart and save
		cart.addProduct(someBook);
		cartRepository.save(cart);

		// 4. Verification Output
		System.out.println("\n--- Cart Verification ---");
		cartRepository.findAll().forEach(c -> {
			System.out.println("Cart ID: " + c.getId());
			c.getProducts().forEach(p -> System.out.println(" - Contains: " + p.toString()));
		});


		// CREATE USERS


		System.out.println("\n--- Creating Users ---");

		// Admin User (Can Add/Edit/Delete books)
		UserEntity admin = new UserEntity(
				"admin",
				passwordEncoder.encode("admin"),  // BCrypt encoded password
				"ADMIN"
		);
		userRepository.save(admin);
		System.out.println("Saved Admin User: admin / admin");

		// Regular User (Can only View and Add to Cart)
		UserEntity user = new UserEntity(
				"user",
				passwordEncoder.encode("user"),   // BCrypt encoded password
				"USER"
		);
		userRepository.save(user);
		System.out.println("Saved Regular User: user / user");

		System.out.println("\nDefault users created successfully!");
		System.out.println("  → Admin login: admin/admin (can add/edit/delete books)");
		System.out.println("  → User login:  user/user   (can only view and add to cart)");
	}
}