## MySQL File Link:
https://drive.google.com/drive/folders/1yb623slbtJVx0KvlAJCL0Ym7YTnklxyt?usp=sharing

## 📦 Installation & First Run

### 1. Clone
Open Terminal or Git Bash then type this to download:
```bash
git clone https://github.com/your‑org/motorph‑payroll.git
```

### 2. Import Database  
1. Open **MySQL Workbench** → *Server ▸ Data Import*.  
2. Select the SQL dump from **Milestone 2 SQL File** → import.  
3. Confirm 30+ tables appear under `motorph_payroll`.

### 3. Configure Connection  
Edit `src/main/java/db/DatabaseConnection.java`:
```java
private static final String URL      = "jdbc:mysql://localhost:3306/payrollsystem_db";
private static final String USER     = "root";
private static final String PASSWORD = "your‑mysql‑password"; ⬅️ Please update with your own DB password
```

### 4. Build  
NetBeans ▸ right‑click project ▸ **Clean and Build**.  
Unit tests pass ➜ fat JAR under `target/`.

### 5. Run  
Hit **▶️** or *Right‑click ▸ Run Project*.  
Login with the test credentials and explore.


## 🔐 Default Test Logins

| Role      | User ID   | Email                       | Password              |
|-----------|-----------|-----------------------------|-----------------------|
| IT        | `U10005`  | `ehernandez@motor.ph`       | `Hernandez@10005`     |
| HR        | `U10006`  | `avillanueva@motor.ph`      | `Villanueva@10006`    |
| Manager   | `U10002`  | `alim@motor.ph`             | `Lim@10002`           |
| Finance   | `U10011`  | `asalcedo@motor.ph`         | `Salcedo@10011`       |
| Employee  | `U10008`  | `aromualdez@motor.ph`       | `Romualdez@10008`     |

*Users can log in using either their email address or their User ID.*

## 🏗️ Project Layout
```
db/             Singleton MySQL connector
pojo/           Plain objects (Employee, Attendance, …)
dao/            CRUD interfaces
daoimpl/        JDBC implementations
service/        Business logic (validation, calculations)
ui/             Swing windows (role‑filtered)
ui/base/        Abstract Swing templates & form helpers
util/           SessionManager + misc helpers
reports/        *.jrxml Jasper templates (layout only)
test/           JUnit5 tests – NOT shipped to users
```
