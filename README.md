# AgriSpatial 🌾

## Precision Agriculture Spatial Analytics and Smart Irrigation System

AgriSpatial is a Java-based precision agriculture system that uses advanced spatial data structures and algorithms to analyze agricultural sensor data efficiently.

The system processes information such as **soil moisture, pH, temperature, humidity, nutrient levels, crop health, and water availability** to support spatial analysis, irrigation planning, and fertilizer recommendations.

The project focuses on using **KD-Trees, Orthogonal Range Trees, and Fractional Cascading** to perform efficient spatial queries on agricultural sensor data.

---

## 🎯 Objectives

* Efficiently manage and analyze large collections of agricultural sensor data.
* Perform fast **nearest-sensor queries** using KD-Trees.
* Perform **2D orthogonal range searches** using a Range Tree.
* Use **Fractional Cascading** for efficient related searches across sorted catalogs.
* Identify irrigation zones based on spatial soil-moisture conditions.
* Generate fertilizer recommendations based on pH and nutrient distribution.
* Support dynamic addition and removal of sensors.
* Provide a web-based interface for visualizing agricultural data and query results.

---

## 🧠 Data Structures and Algorithms

| Data Structure / Algorithm | Purpose                                             |
| -------------------------- | --------------------------------------------------- |
| **KD-Tree**                | Efficient nearest-sensor and point-location queries |
| **Orthogonal Range Tree**  | 2D rectangular spatial range queries                |
| **Fractional Cascading**   | Efficient searching across related sorted catalogs  |
| **ArrayList**              | Dynamic storage and management of sensor records    |
| **Binary Search**          | Fast lookup within sorted secondary catalogs        |

### KD-Tree

The KD-Tree organizes sensors according to their spatial coordinates `(x, y)`.

It allows the system to efficiently locate sensors that are geographically closest to a given point while avoiding unnecessary branches of the search tree.

### Orthogonal Range Tree

The Range Tree organizes sensor locations primarily by their **X-coordinate** and maintains secondary catalogs ordered by **Y-coordinate**.

This enables queries such as:

```text
Find all sensors where:

xmin ≤ x ≤ xmax
ymin ≤ y ≤ ymax
```

### Fractional Cascading

Fractional Cascading is implemented using augmented sorted catalogs and bridge pointers.

The system performs an initial binary search and then uses bridge relationships to efficiently locate corresponding positions in related catalogs.

---

## 🏗️ System Architecture

```text
                    Agricultural Sensor Data
                              │
                              ▼
                     farm_sensors.csv
                              │
                              ▼
                         CSVLoader
                              │
                              ▼
                           Sensor
                              │
                              ▼
                       SensorManager
                              │
             ┌────────────────┼────────────────┐
             ▼                ▼                ▼
          KD-Tree         Range Tree      Fractional
                                           Cascading
             │                │                │
             └────────────────┼────────────────┘
                              ▼
                 Agricultural Analysis
                              │
             ┌────────────────┼────────────────┐
             ▼                ▼                ▼
       Nearest Sensor    Range Queries    Moisture Search
             │                │                │
             └────────────────┼────────────────┘
                              ▼
                Irrigation & Soil Analysis
                              │
             ┌────────────────┴────────────────┐
             ▼                                 ▼
      Irrigation Zones                  Fertilizer Advice
             │                                 │
             └────────────────┬────────────────┘
                              ▼
                     Web-based Interface
```

---

## 📁 Project Structure

```text
AgriSpatial/
│
├── data/
│   └── farm_sensors.csv
│
├── src/
│   ├── Main.java
│   ├── Sensor.java
│   ├── SensorManager.java
│   ├── CSVLoader.java
│   │
│   ├── KDNode.java
│   ├── KDTree.java
│   ├── RangeIndex.java
│   ├── FractionalCascade.java
│   │
│   ├── IrrigationService.java
│   └── FertilizerService.java
│
├── web/
│   ├── index.html
│   ├── field.html
│   ├── soil.html
│   ├── irrigation.html
│   ├── algorithms.html
│   ├── app.js
│   └── styles.css
|
|
├── results_ppt/
│   ├── AgriSpatial_DSA.pptx
│
├── report/
│   ├── DSA_Report.docx
│
└── README.md
```

---

## ⚙️ Technologies Used

* **Java** – Core application and algorithm implementation
* **Java Collections Framework** – Data storage and manipulation
* **HTML** – Web interface structure
* **CSS** – Interface styling
* **JavaScript** – Frontend interaction and API communication
* **Java HTTP Server** – Lightweight backend server
* **CSV** – Agricultural sensor dataset

---

## 📊 Sensor Data

Each sensor record contains spatial and agricultural information.

Example attributes include:

```text
Sensor ID
X Coordinate
Y Coordinate
Soil Moisture
pH
Temperature
Humidity
Nutrient Level
Crop Health
Water Availability
```

The spatial coordinates represent sensor locations within the agricultural field.

---

## 🔍 Main Features

### 1. Nearest Sensor Search

The user can select a point on the field and retrieve the nearest agricultural sensor using the KD-Tree.

### 2. Orthogonal Range Search

The system allows users to define a rectangular region:

```text
Xmin ─ Xmax
Ymin ─ Ymax
```

All sensors located inside the selected region are returned.

### 3. Fractional Cascading Search

The system supports moisture-based searches across multiple related catalogs using fractional cascading.

### 4. Irrigation Zone Analysis

The agricultural field is divided into spatial zones and analyzed using soil moisture information.

Each zone can be classified as:

* **Irrigate Now**
* **Monitor**
* **Healthy**

### 5. Fertilizer Recommendation

The system analyzes:

* Soil pH
* Nutrient levels
* Spatial distribution

and provides fertilizer-related recommendations.

### 6. Dynamic Sensor Management

Sensors can be added or removed from the system.

After a modification, the spatial indexes are rebuilt to maintain consistency with the updated dataset.

---

## 🚀 How to Run

### Prerequisites

Install:

* Java JDK 11 or later
* A web browser
* Git (optional, for cloning the repository)

Check your Java installation:

```bash
java -version
javac -version
```

---

### Step 1 — Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/AgriSpatial.git
```

Navigate into the project:

```bash
cd AgriSpatial
```

---

### Step 2 — Compile the Java Source Files

From the project root:

```bash
javac -d out src/*.java
```

---

### Step 3 — Start the Server

```bash
java -cp out Main
```

The application starts a local HTTP server.

Open:

```text
http://localhost:8080
```

in your browser.

---

## 🌐 Web Interface

The project contains multiple web pages:

| Page               | Purpose                                  |
| ------------------ | ---------------------------------------- |
| Dashboard          | Overall agricultural field summary       |
| Field Explorer     | Spatial queries and sensor visualization |
| Soil Intelligence  | pH, nutrients and fertilizer analysis    |
| Irrigation Planner | Irrigation zone analysis                 |
| Algorithm Lab      | Demonstration of implemented algorithms  |

---

## 🧪 Example Queries

### Nearest Sensor

Given a point:

```text
(x, y) = (45, 60)
```

the KD-Tree searches for the closest sensor without scanning every sensor.

### Orthogonal Range Query

Example:

```text
X: 20 – 60
Y: 30 – 80
```

The Range Tree returns sensors located inside the specified rectangular region.

### Moisture Search

A moisture target can be supplied to search the related sorted catalogs using the Fractional Cascading structure.

---

## ⏱️ Complexity

The project is designed to demonstrate efficient spatial searching using specialized data structures.

| Operation                  | Data Structure       | Expected Complexity          |
| -------------------------- | -------------------- | ---------------------------- |
| Nearest-neighbor search    | KD-Tree              | Average: `O(log n)`          |
| 2D orthogonal range search | Range Tree           | `O(log² n + k)`              |
| Initial sorted search      | Binary Search        | `O(log n)`                   |
| Related catalog search     | Fractional Cascading | Approximately `O(log n + k)` |
| Sequential scan baseline   | Array/List           | `O(n)`                       |

Here, `n` represents the number of sensors and `k` represents the number of sensors returned by a query.

Actual performance depends on the dataset, query distribution, and implementation details.

---

## 📈 Experimental Evaluation

The system can be evaluated using:

* Different numbers of sensor records
* Nearest-neighbor queries
* Rectangular range queries
* Moisture-based searches
* Sensor insertion and deletion
* Number of KD-Tree nodes visited
* Number of Range Tree catalogs visited
* Execution time

The experimental results can be used to compare indexed spatial searching against conventional sequential searching.

---

## 🔄 Dynamic Updates

The system supports:

```text
Add Sensor
     │
     ▼
Update Sensor Collection
     │
     ▼
Rebuild Spatial Indexes
     │
     ├── KD-Tree
     ├── Range Tree
     └── Fractional Cascading
```

This ensures that subsequent queries operate on the updated sensor dataset.

---

## 🌱 Applications

AgriSpatial can support:

* Precision agriculture
* Smart irrigation planning
* Soil condition analysis
* Crop monitoring
* Fertilizer planning
* Spatial sensor analysis
* Agricultural decision support

---

## 🔮 Future Scope

Possible improvements include:

* Integration with real-time IoT sensor streams
* Larger agricultural datasets
* Dynamic index updates without complete rebuilding
* Advanced crop-health prediction
* Integration with satellite imagery
* Machine-learning-based irrigation prediction
* More detailed GIS-based field visualization
* Cloud deployment for remote access

---

## 👥 Project Information

**Project:** Precision Agriculture Spatial Analytics and Smart Irrigation System

**Course:** Data Structures and Algorithms

**Domain:** Precision Agriculture / Spatial Analytics

**Core Concepts:**

```text
KD-Tree
Orthogonal Range Tree
Fractional Cascading
Spatial Searching
Nearest-Neighbor Search
Range Searching
```

---

## 📜 License

This project was developed for academic and educational purposes.
