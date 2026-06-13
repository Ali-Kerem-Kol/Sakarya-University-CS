import csv
import random
from faker import Faker

fake = Faker()
random.seed(42)
Faker.seed(42)

CATEGORIES = ["Electronics", "Clothing", "Home & Garden", "Sports", "Books",
              "Toys", "Food", "Beauty", "Automotive", "Health"]
COUNTRIES = ["Turkey", "USA", "Germany", "UK", "France", "Italy", "Spain",
             "Netherlands", "Japan", "Canada"]
PAYMENT_METHODS = ["Credit Card", "Debit Card", "PayPal", "Bank Transfer", "Cash"]

NUM_CUSTOMERS = 10000
NUM_PRODUCTS = 5000
NUM_ORDERS = 1_000_000


def generate_customers(filename="customers.csv"):
    print(f"Generating {NUM_CUSTOMERS} customers...")
    with open(filename, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["customer_id", "name", "email", "age", "country", "city",
                          "registration_date"])
        for i in range(1, NUM_CUSTOMERS + 1):
            writer.writerow([
                i,
                fake.name(),
                fake.email(),
                random.randint(18, 75),
                random.choice(COUNTRIES),
                fake.city(),
                fake.date_between(start_date="-5y", end_date="today").isoformat(),
            ])
    print(f"  -> {filename} created.")


def generate_products(filename="products.csv"):
    print(f"Generating {NUM_PRODUCTS} products...")
    with open(filename, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["product_id", "product_name", "category", "price", "stock"])
        for i in range(1, NUM_PRODUCTS + 1):
            writer.writerow([
                i,
                fake.catch_phrase(),
                random.choice(CATEGORIES),
                round(random.uniform(1.99, 999.99), 2),
                random.randint(0, 5000),
            ])
    print(f"  -> {filename} created.")


def generate_orders(filename="orders.csv"):
    print(f"Generating {NUM_ORDERS} orders...")
    with open(filename, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["order_id", "customer_id", "product_id", "quantity",
                          "total_price", "order_date", "payment_method", "status"])
        for i in range(1, NUM_ORDERS + 1):
            product_price = round(random.uniform(1.99, 999.99), 2)
            qty = random.randint(1, 10)
            writer.writerow([
                i,
                random.randint(1, NUM_CUSTOMERS),
                random.randint(1, NUM_PRODUCTS),
                qty,
                round(product_price * qty, 2),
                fake.date_between(start_date="-3y", end_date="today").isoformat(),
                random.choice(PAYMENT_METHODS),
                random.choice(["Completed", "Pending", "Cancelled", "Shipped"]),
            ])
            if i % 200000 == 0:
                print(f"  {i}/{NUM_ORDERS} orders generated...")
    print(f"  -> {filename} created.")


if __name__ == "__main__":
    generate_customers()
    generate_products()
    generate_orders()
    print("All data files generated successfully!")
