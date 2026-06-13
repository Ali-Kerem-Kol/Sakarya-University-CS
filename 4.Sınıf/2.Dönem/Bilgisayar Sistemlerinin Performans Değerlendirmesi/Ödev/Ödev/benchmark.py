import csv
import time
import json
import os
import mysql.connector
from pymongo import MongoClient
import matplotlib.pyplot as plt
import matplotlib
matplotlib.use("Agg")
import numpy as np

MYSQL_CONFIG = {"host": "localhost", "port": 3306, "user": "root", "password": ""}
MONGO_URI = "mongodb://localhost:27017"
DB_NAME = "benchmark_db"

MYSQL_BIN = r"C:\Program Files\MySQL\MySQL Server 8.4\bin"

SIZES = [10_000, 100_000, 1_000_000]
RESULTS = {}


def read_csv(filename, limit=None):
    rows = []
    with open(filename, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for i, row in enumerate(reader):
            if limit and i >= limit:
                break
            rows.append(row)
    return rows


# ─── MySQL helpers ────────────────────────────────────────────────────────────

def mysql_connect():
    return mysql.connector.connect(**MYSQL_CONFIG)


def mysql_setup():
    conn = mysql_connect()
    cur = conn.cursor()
    cur.execute(f"DROP DATABASE IF EXISTS {DB_NAME}")
    cur.execute(f"CREATE DATABASE {DB_NAME}")
    cur.execute(f"USE {DB_NAME}")
    cur.execute("""
        CREATE TABLE customers (
            customer_id INT PRIMARY KEY,
            name VARCHAR(100),
            email VARCHAR(100),
            age INT,
            country VARCHAR(50),
            city VARCHAR(80),
            registration_date DATE
        ) ENGINE=InnoDB
    """)
    cur.execute("""
        CREATE TABLE products (
            product_id INT PRIMARY KEY,
            product_name VARCHAR(150),
            category VARCHAR(50),
            price DECIMAL(10,2),
            stock INT
        ) ENGINE=InnoDB
    """)
    cur.execute("""
        CREATE TABLE orders (
            order_id INT PRIMARY KEY,
            customer_id INT,
            product_id INT,
            quantity INT,
            total_price DECIMAL(10,2),
            order_date DATE,
            payment_method VARCHAR(30),
            status VARCHAR(20),
            INDEX idx_customer (customer_id),
            INDEX idx_product (product_id),
            INDEX idx_date (order_date),
            FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
            FOREIGN KEY (product_id) REFERENCES products(product_id)
        ) ENGINE=InnoDB
    """)
    conn.commit()
    cur.close()
    conn.close()


def mysql_insert(table, rows, batch_size=5000):
    conn = mysql_connect()
    cur = conn.cursor()
    cur.execute(f"USE {DB_NAME}")

    if table == "customers":
        sql = "INSERT INTO customers VALUES (%s,%s,%s,%s,%s,%s,%s)"
        data = [(int(r["customer_id"]), r["name"], r["email"], int(r["age"]),
                 r["country"], r["city"], r["registration_date"]) for r in rows]
    elif table == "products":
        sql = "INSERT INTO products VALUES (%s,%s,%s,%s,%s)"
        data = [(int(r["product_id"]), r["product_name"], r["category"],
                 float(r["price"]), int(r["stock"])) for r in rows]
    else:
        sql = "INSERT INTO orders VALUES (%s,%s,%s,%s,%s,%s,%s,%s)"
        data = [(int(r["order_id"]), int(r["customer_id"]), int(r["product_id"]),
                 int(r["quantity"]), float(r["total_price"]), r["order_date"],
                 r["payment_method"], r["status"]) for r in rows]

    for i in range(0, len(data), batch_size):
        cur.executemany(sql, data[i:i+batch_size])
    conn.commit()
    cur.close()
    conn.close()


def mysql_select_single(order_id):
    conn = mysql_connect()
    cur = conn.cursor()
    cur.execute(f"USE {DB_NAME}")
    cur.execute("SELECT * FROM orders WHERE order_id = %s", (order_id,))
    cur.fetchall()
    cur.close()
    conn.close()


def mysql_select_range(min_age, max_age):
    conn = mysql_connect()
    cur = conn.cursor()
    cur.execute(f"USE {DB_NAME}")
    cur.execute("""
        SELECT o.* FROM orders o
        JOIN customers c ON o.customer_id = c.customer_id
        WHERE c.age BETWEEN %s AND %s
    """, (min_age, max_age))
    result = cur.fetchall()
    cur.close()
    conn.close()
    return len(result)


def mysql_update(order_id_start, order_id_end):
    conn = mysql_connect()
    cur = conn.cursor()
    cur.execute(f"USE {DB_NAME}")
    cur.execute("""
        UPDATE orders SET status = 'Updated'
        WHERE order_id BETWEEN %s AND %s
    """, (order_id_start, order_id_end))
    affected = cur.rowcount
    conn.commit()
    cur.close()
    conn.close()
    return affected


def mysql_delete(order_id_start, order_id_end):
    conn = mysql_connect()
    cur = conn.cursor()
    cur.execute(f"USE {DB_NAME}")
    cur.execute("""
        DELETE FROM orders WHERE order_id BETWEEN %s AND %s
    """, (order_id_start, order_id_end))
    affected = cur.rowcount
    conn.commit()
    cur.close()
    conn.close()
    return affected


def mysql_complex_query():
    conn = mysql_connect()
    cur = conn.cursor()
    cur.execute(f"USE {DB_NAME}")
    cur.execute("""
        SELECT c.country, p.category,
               COUNT(*) as order_count,
               SUM(o.total_price) as total_revenue,
               AVG(o.total_price) as avg_order_value
        FROM orders o
        JOIN customers c ON o.customer_id = c.customer_id
        JOIN products p ON o.product_id = p.product_id
        GROUP BY c.country, p.category
        ORDER BY total_revenue DESC
        LIMIT 20
    """)
    result = cur.fetchall()
    cur.close()
    conn.close()
    return len(result)


# ─── MongoDB helpers ──────────────────────────────────────────────────────────

def mongo_connect():
    client = MongoClient(MONGO_URI)
    return client


def mongo_setup():
    client = mongo_connect()
    client.drop_database(DB_NAME)
    db = client[DB_NAME]
    db.create_collection("orders")
    db["orders"].create_index("order_id")
    db["orders"].create_index("customer_id")
    db["orders"].create_index("product_id")
    db["orders"].create_index("order_date")
    db.create_collection("customers")
    db["customers"].create_index("customer_id")
    db["customers"].create_index("age")
    db.create_collection("products")
    db["products"].create_index("product_id")
    client.close()


def mongo_insert(collection_name, rows, batch_size=5000):
    client = mongo_connect()
    db = client[DB_NAME]
    col = db[collection_name]

    if collection_name == "customers":
        docs = [{"customer_id": int(r["customer_id"]), "name": r["name"],
                 "email": r["email"], "age": int(r["age"]),
                 "country": r["country"], "city": r["city"],
                 "registration_date": r["registration_date"]} for r in rows]
    elif collection_name == "products":
        docs = [{"product_id": int(r["product_id"]),
                 "product_name": r["product_name"],
                 "category": r["category"],
                 "price": float(r["price"]),
                 "stock": int(r["stock"])} for r in rows]
    else:
        docs = [{"order_id": int(r["order_id"]),
                 "customer_id": int(r["customer_id"]),
                 "product_id": int(r["product_id"]),
                 "quantity": int(r["quantity"]),
                 "total_price": float(r["total_price"]),
                 "order_date": r["order_date"],
                 "payment_method": r["payment_method"],
                 "status": r["status"]} for r in rows]

    for i in range(0, len(docs), batch_size):
        col.insert_many(docs[i:i+batch_size])
    client.close()


def mongo_select_single(order_id):
    client = mongo_connect()
    db = client[DB_NAME]
    db["orders"].find_one({"order_id": order_id})
    client.close()


def mongo_select_range(min_age, max_age):
    client = mongo_connect()
    db = client[DB_NAME]
    customer_ids = [c["customer_id"] for c in
                    db["customers"].find({"age": {"$gte": min_age, "$lte": max_age}},
                                         {"customer_id": 1})]
    result = list(db["orders"].find({"customer_id": {"$in": customer_ids}}))
    client.close()
    return len(result)


def mongo_update(order_id_start, order_id_end):
    client = mongo_connect()
    db = client[DB_NAME]
    result = db["orders"].update_many(
        {"order_id": {"$gte": order_id_start, "$lte": order_id_end}},
        {"$set": {"status": "Updated"}}
    )
    client.close()
    return result.modified_count


def mongo_delete(order_id_start, order_id_end):
    client = mongo_connect()
    db = client[DB_NAME]
    result = db["orders"].delete_many(
        {"order_id": {"$gte": order_id_start, "$lte": order_id_end}}
    )
    client.close()
    return result.deleted_count


def mongo_complex_query():
    client = mongo_connect()
    db = client[DB_NAME]
    pipeline = [
        {"$lookup": {
            "from": "customers",
            "localField": "customer_id",
            "foreignField": "customer_id",
            "as": "customer"
        }},
        {"$unwind": "$customer"},
        {"$lookup": {
            "from": "products",
            "localField": "product_id",
            "foreignField": "product_id",
            "as": "product"
        }},
        {"$unwind": "$product"},
        {"$group": {
            "_id": {"country": "$customer.country", "category": "$product.category"},
            "order_count": {"$sum": 1},
            "total_revenue": {"$sum": "$total_price"},
            "avg_order_value": {"$avg": "$total_price"}
        }},
        {"$sort": {"total_revenue": -1}},
        {"$limit": 20}
    ]
    result = list(db["orders"].aggregate(pipeline, allowDiskUse=True))
    client.close()
    return len(result)


# ─── Benchmark runner ─────────────────────────────────────────────────────────

def timed(func, *args, **kwargs):
    start = time.perf_counter()
    result = func(*args, **kwargs)
    elapsed = time.perf_counter() - start
    return elapsed, result


def run_insert_benchmark():
    print("\n" + "=" * 60)
    print("INSERT (Write) BENCHMARK")
    print("=" * 60)

    customers = read_csv("customers.csv")
    products = read_csv("products.csv")
    all_orders = read_csv("orders.csv")

    results = {"mysql": {}, "mongodb": {}}

    for size in SIZES:
        orders = all_orders[:size]
        label = f"{size // 1000}K" if size < 1_000_000 else "1M"
        print(f"\n--- {label} records ---")

        # MySQL
        mysql_setup()
        mysql_insert("customers", customers)
        mysql_insert("products", products)
        elapsed, _ = timed(mysql_insert, "orders", orders)
        results["mysql"][label] = elapsed
        print(f"  MySQL  INSERT {label}: {elapsed:.3f}s")

        # MongoDB
        mongo_setup()
        mongo_insert("customers", customers)
        mongo_insert("products", products)
        elapsed, _ = timed(mongo_insert, "orders", orders)
        results["mongodb"][label] = elapsed
        print(f"  MongoDB INSERT {label}: {elapsed:.3f}s")

    return results


def run_select_benchmark():
    print("\n" + "=" * 60)
    print("SELECT (Read) BENCHMARK")
    print("=" * 60)

    results = {"single": {"mysql": 0, "mongodb": 0},
               "range": {"mysql": 0, "mongodb": 0}}

    # Single record query (average of 100 queries)
    ids = [1, 500, 5000, 50000, 100000, 250000, 500000, 750000, 999999, 1000000]

    print("\n--- Single Record Query (avg of 10 queries) ---")
    total_mysql = 0
    for oid in ids:
        elapsed, _ = timed(mysql_select_single, oid)
        total_mysql += elapsed
    results["single"]["mysql"] = total_mysql / len(ids)
    print(f"  MySQL  avg: {results['single']['mysql']*1000:.2f}ms")

    total_mongo = 0
    for oid in ids:
        elapsed, _ = timed(mongo_select_single, oid)
        total_mongo += elapsed
    results["single"]["mongodb"] = total_mongo / len(ids)
    print(f"  MongoDB avg: {results['single']['mongodb']*1000:.2f}ms")

    # Range query
    print("\n--- Range Query (age 25-35) ---")
    elapsed, count = timed(mysql_select_range, 25, 35)
    results["range"]["mysql"] = elapsed
    print(f"  MySQL  range query: {elapsed:.3f}s ({count} rows)")

    elapsed, count = timed(mongo_select_range, 25, 35)
    results["range"]["mongodb"] = elapsed
    print(f"  MongoDB range query: {elapsed:.3f}s ({count} docs)")

    return results


def run_update_benchmark():
    print("\n" + "=" * 60)
    print("UPDATE BENCHMARK")
    print("=" * 60)

    sizes = [(1, 1000), (1001, 10000), (10001, 100000)]
    labels = ["1K", "10K", "100K"]
    results = {"mysql": {}, "mongodb": {}}

    for (start, end), label in zip(sizes, labels):
        print(f"\n--- Update {label} records ---")
        elapsed, affected = timed(mysql_update, start, end)
        results["mysql"][label] = elapsed
        print(f"  MySQL  UPDATE {label}: {elapsed:.3f}s ({affected} rows)")

        elapsed, affected = timed(mongo_update, start, end)
        results["mongodb"][label] = elapsed
        print(f"  MongoDB UPDATE {label}: {elapsed:.3f}s ({affected} docs)")

    return results


def run_delete_benchmark():
    print("\n" + "=" * 60)
    print("DELETE BENCHMARK")
    print("=" * 60)

    sizes = [(900001, 910000), (800001, 900000), (500001, 800000)]
    labels = ["10K", "100K", "300K"]
    results = {"mysql": {}, "mongodb": {}}

    for (start, end), label in zip(sizes, labels):
        count = end - start + 1
        print(f"\n--- Delete {label} records ---")
        elapsed, affected = timed(mysql_delete, start, end)
        results["mysql"][label] = elapsed
        print(f"  MySQL  DELETE {label}: {elapsed:.3f}s ({affected} rows)")

        elapsed, affected = timed(mongo_delete, start, end)
        results["mongodb"][label] = elapsed
        print(f"  MongoDB DELETE {label}: {elapsed:.3f}s ({affected} docs)")

    return results


def run_complex_benchmark():
    print("\n" + "=" * 60)
    print("COMPLEX QUERY BENCHMARK")
    print("=" * 60)

    results = {"mysql": 0, "mongodb": 0}

    print("\n--- JOIN / Aggregation Pipeline ---")
    print("  (Country + Category revenue analysis)")

    elapsed, count = timed(mysql_complex_query)
    results["mysql"] = elapsed
    print(f"  MySQL  (JOIN): {elapsed:.3f}s ({count} result rows)")

    elapsed, count = timed(mongo_complex_query)
    results["mongodb"] = elapsed
    print(f"  MongoDB (Aggregation): {elapsed:.3f}s ({count} result docs)")

    return results


# ─── Charts ───────────────────────────────────────────────────────────────────

def create_charts(insert_res, select_res, update_res, delete_res, complex_res):
    os.makedirs("charts", exist_ok=True)
    plt.rcParams.update({"font.size": 12})

    # 1) Insert benchmark
    fig, ax = plt.subplots(figsize=(10, 6))
    labels = list(insert_res["mysql"].keys())
    mysql_vals = [insert_res["mysql"][l] for l in labels]
    mongo_vals = [insert_res["mongodb"][l] for l in labels]
    x = np.arange(len(labels))
    w = 0.35
    ax.bar(x - w/2, mysql_vals, w, label="MySQL", color="#4479A1")
    ax.bar(x + w/2, mongo_vals, w, label="MongoDB", color="#47A248")
    ax.set_xlabel("Record Count")
    ax.set_ylabel("Time (seconds)")
    ax.set_title("INSERT Performance Comparison")
    ax.set_xticks(x)
    ax.set_xticklabels(labels)
    ax.legend()
    for i, (m, mo) in enumerate(zip(mysql_vals, mongo_vals)):
        ax.text(i - w/2, m + 0.1, f"{m:.2f}s", ha="center", fontsize=9)
        ax.text(i + w/2, mo + 0.1, f"{mo:.2f}s", ha="center", fontsize=9)
    plt.tight_layout()
    plt.savefig("charts/insert_benchmark.png", dpi=150)
    plt.close()

    # 2) Select benchmark
    fig, axes = plt.subplots(1, 2, figsize=(14, 6))
    # Single
    ax = axes[0]
    vals = [select_res["single"]["mysql"] * 1000, select_res["single"]["mongodb"] * 1000]
    bars = ax.bar(["MySQL", "MongoDB"], vals, color=["#4479A1", "#47A248"])
    ax.set_ylabel("Time (ms)")
    ax.set_title("Single Record Query (avg)")
    for bar, v in zip(bars, vals):
        ax.text(bar.get_x() + bar.get_width()/2, v + 0.05, f"{v:.2f}ms",
                ha="center", fontsize=10)
    # Range
    ax = axes[1]
    vals = [select_res["range"]["mysql"], select_res["range"]["mongodb"]]
    bars = ax.bar(["MySQL", "MongoDB"], vals, color=["#4479A1", "#47A248"])
    ax.set_ylabel("Time (seconds)")
    ax.set_title("Range Query (age 25-35)")
    for bar, v in zip(bars, vals):
        ax.text(bar.get_x() + bar.get_width()/2, v + 0.05, f"{v:.2f}s",
                ha="center", fontsize=10)
    plt.tight_layout()
    plt.savefig("charts/select_benchmark.png", dpi=150)
    plt.close()

    # 3) Update benchmark
    fig, ax = plt.subplots(figsize=(10, 6))
    labels = list(update_res["mysql"].keys())
    mysql_vals = [update_res["mysql"][l] for l in labels]
    mongo_vals = [update_res["mongodb"][l] for l in labels]
    x = np.arange(len(labels))
    ax.bar(x - w/2, mysql_vals, w, label="MySQL", color="#4479A1")
    ax.bar(x + w/2, mongo_vals, w, label="MongoDB", color="#47A248")
    ax.set_xlabel("Record Count")
    ax.set_ylabel("Time (seconds)")
    ax.set_title("UPDATE Performance Comparison")
    ax.set_xticks(x)
    ax.set_xticklabels(labels)
    ax.legend()
    for i, (m, mo) in enumerate(zip(mysql_vals, mongo_vals)):
        ax.text(i - w/2, m + 0.01, f"{m:.3f}s", ha="center", fontsize=9)
        ax.text(i + w/2, mo + 0.01, f"{mo:.3f}s", ha="center", fontsize=9)
    plt.tight_layout()
    plt.savefig("charts/update_benchmark.png", dpi=150)
    plt.close()

    # 4) Delete benchmark
    fig, ax = plt.subplots(figsize=(10, 6))
    labels = list(delete_res["mysql"].keys())
    mysql_vals = [delete_res["mysql"][l] for l in labels]
    mongo_vals = [delete_res["mongodb"][l] for l in labels]
    x = np.arange(len(labels))
    ax.bar(x - w/2, mysql_vals, w, label="MySQL", color="#4479A1")
    ax.bar(x + w/2, mongo_vals, w, label="MongoDB", color="#47A248")
    ax.set_xlabel("Record Count")
    ax.set_ylabel("Time (seconds)")
    ax.set_title("DELETE Performance Comparison")
    ax.set_xticks(x)
    ax.set_xticklabels(labels)
    ax.legend()
    for i, (m, mo) in enumerate(zip(mysql_vals, mongo_vals)):
        ax.text(i - w/2, m + 0.01, f"{m:.3f}s", ha="center", fontsize=9)
        ax.text(i + w/2, mo + 0.01, f"{mo:.3f}s", ha="center", fontsize=9)
    plt.tight_layout()
    plt.savefig("charts/delete_benchmark.png", dpi=150)
    plt.close()

    # 5) Complex query
    fig, ax = plt.subplots(figsize=(8, 6))
    vals = [complex_res["mysql"], complex_res["mongodb"]]
    bars = ax.bar(["MySQL (JOIN)", "MongoDB (Aggregation)"], vals,
                  color=["#4479A1", "#47A248"])
    ax.set_ylabel("Time (seconds)")
    ax.set_title("Complex Query Performance Comparison")
    for bar, v in zip(bars, vals):
        ax.text(bar.get_x() + bar.get_width()/2, v + 0.1, f"{v:.2f}s",
                ha="center", fontsize=11)
    plt.tight_layout()
    plt.savefig("charts/complex_benchmark.png", dpi=150)
    plt.close()

    # 6) Summary / Overall
    fig, ax = plt.subplots(figsize=(12, 7))
    categories = ["INSERT\n(1M)", "SELECT\n(Single)", "SELECT\n(Range)",
                   "UPDATE\n(100K)", "DELETE\n(300K)", "Complex\nQuery"]
    mysql_all = [
        insert_res["mysql"]["1M"],
        select_res["single"]["mysql"] * 1000,
        select_res["range"]["mysql"],
        update_res["mysql"]["100K"],
        delete_res["mysql"]["300K"],
        complex_res["mysql"]
    ]
    mongo_all = [
        insert_res["mongodb"]["1M"],
        select_res["single"]["mongodb"] * 1000,
        select_res["range"]["mongodb"],
        update_res["mongodb"]["100K"],
        delete_res["mongodb"]["300K"],
        complex_res["mongodb"]
    ]
    x = np.arange(len(categories))
    ax.bar(x - w/2, mysql_all, w, label="MySQL", color="#4479A1")
    ax.bar(x + w/2, mongo_all, w, label="MongoDB", color="#47A248")
    ax.set_ylabel("Time (seconds / ms for single select)")
    ax.set_title("Overall Performance Comparison: MySQL vs MongoDB")
    ax.set_xticks(x)
    ax.set_xticklabels(categories)
    ax.legend()
    plt.tight_layout()
    plt.savefig("charts/overall_comparison.png", dpi=150)
    plt.close()

    print("\nAll charts saved to 'charts/' directory.")


# ─── Throughput calculation ───────────────────────────────────────────────────

def calculate_throughput(insert_res):
    print("\n" + "=" * 60)
    print("THROUGHPUT (records/second)")
    print("=" * 60)
    throughput = {"mysql": {}, "mongodb": {}}
    for label in insert_res["mysql"]:
        size = int(label.replace("K", "000").replace("M", "000000")
                   .replace("0000000", "1000000"))
        if label == "1M":
            size = 1_000_000
        elif label == "100K":
            size = 100_000
        elif label == "10K":
            size = 10_000
        mysql_tp = size / insert_res["mysql"][label]
        mongo_tp = size / insert_res["mongodb"][label]
        throughput["mysql"][label] = mysql_tp
        throughput["mongodb"][label] = mongo_tp
        print(f"  {label}: MySQL={mysql_tp:,.0f} rec/s | MongoDB={mongo_tp:,.0f} rec/s")
    return throughput


# ─── Main ─────────────────────────────────────────────────────────────────────

def main():
    print("=" * 60)
    print("MySQL vs MongoDB Performance Benchmark")
    print("=" * 60)

    # Generate data if not exists
    if not os.path.exists("orders.csv"):
        print("Data files not found. Run generate_data.py first.")
        return

    insert_results = run_insert_benchmark()
    throughput = calculate_throughput(insert_results)

    # Re-setup with full 1M data for remaining tests
    print("\n>>> Re-loading 1M records for SELECT/UPDATE/DELETE tests...")
    mysql_setup()
    customers = read_csv("customers.csv")
    products = read_csv("products.csv")
    orders = read_csv("orders.csv")
    mysql_insert("customers", customers)
    mysql_insert("products", products)
    mysql_insert("orders", orders)
    mongo_setup()
    mongo_insert("customers", customers)
    mongo_insert("products", products)
    mongo_insert("orders", orders)
    print(">>> Data loaded.")

    select_results = run_select_benchmark()
    update_results = run_update_benchmark()
    delete_results = run_delete_benchmark()
    complex_results = run_complex_benchmark()

    # Save raw results
    all_results = {
        "insert": insert_results,
        "throughput": throughput,
        "select": select_results,
        "update": update_results,
        "delete": delete_results,
        "complex": complex_results,
    }
    with open("benchmark_results.json", "w") as f:
        json.dump(all_results, f, indent=2)
    print("\nResults saved to benchmark_results.json")

    create_charts(insert_results, select_results, update_results,
                  delete_results, complex_results)

    print("\n" + "=" * 60)
    print("BENCHMARK COMPLETE!")
    print("=" * 60)


if __name__ == "__main__":
    main()
