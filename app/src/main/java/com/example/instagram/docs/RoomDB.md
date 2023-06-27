## Drawback of SQLite:
- We have to rectify the query manually if the schema gets changed.
- A huge number of pre-defined codes need to be declared to convert the SQL queries into Java Objects.

## Advantages of Room:
- Compile-time verification of SQL queries.
- Convenience annotations that minimize repetitive and error-prone boilerplate code.


## Room Database: 
**There are three major components in Room:**
1. **Database class**: Holds the database and serves as the main access point.
2. **Data entities**: that represent tables in your app's database.
3. **DAO**: provides methods that your app can use to query, update, insert, and delete data in the database.

# Defining data using Room entities - P1
You define entities to represent the objects that you want to store.
Each entity corresponds to a table in the associated Room database, and each instance of an entity represents a row of data in the corresponding table.

> **Anatomy of an entity**
> ```kotlin
> @Entity(tableName = "users")
> data class User(
>    @PrimaryKey(autoGenerate = true) val id: Int,
>    @ColumnInfo(name = "first_name") val firstName: String?,
>    @ColumnInfo(name = "last_name") val lastName: String?
> )
> ```
> By default, Room uses the class name as the database table name. If you want the table to have a different name, set the `tableName` property of the `@Entity` annotation. Similarly, Room uses the field names as column names in the database by default. If you want a column to have a different name, add the `@ColumnInfo` annotation to the field and set the `name` property.

> **Define a primary key**
> 
> Each Room entity **must** define a primary key that uniquely identifies each row in the corresponding 
> database table. The most straightforward way of doing this is to annotate a single column with `@PrimaryKey`.
> If you need Room to assign automatic IDs to entity instances, set the `autoGenerate` property of `@PrimaryKey` to true.

> **Ignore fields**
> 
> By default, Room creates a column for each field that's defined in the entity. If an entity has fields that you don't want to persist, you can annotate them using `@Ignore`.

> **Support full-text search**
> 
> To use Fts, add the `@Fts3` or `@Fts4` annotation to a given entity.
> 
> > Note: FTS-enabled tables always use a primary key of type `INTEGER` and with the column name `"rowid"`. If your FTS-table-backed entity defines a primary key, it must use that type and column name.

# Accessing data using Room DAOs - P2

There are two types of DAO methods that define database interactions:
- Convenience methods that let you insert, update, and delete rows in your database without writing any SQL code.
- Query methods that let you write your own SQL query to interact with the database.

### Convenience methods

> **Insert**:
> 
> Each parameter for an `@Insert` method must be either an instance of a Room data entity class annotated with `@Entity`.
> 
> If the `@Insert` method receives a single parameter, it can return a `long` value, which is the new `rowId` for the inserted item. If the parameter is an `array` or a `collection`, then return an `array or a collection of long` values instead, with each value as the `rowId` for one of the inserted items.

> **Update:**
> - The `@Update` annotation lets you define methods that update specific rows in a database table. Like` @Insert` methods, `@Update` methods accept data entity instances as parameters.
> - Room uses the `primary key` to match passed entity instances to rows in the database. If there is no row with the same `primary key`, Room makes no changes.
> - An `@Update` method can optionally return an `int` value indicating the number of rows that were updated successfully.

> **Delete**
> 
> - Like `@Insert` methods, `@Delete` methods accept data entity instances as parameters.
> - Room uses the `primary key` to match passed entity instances to rows in the database. If there is no row with the `same primary` key, Room makes no changes.
> - A `@Delete` method can optionally return an `int` value indicating the number of rows that were deleted successfully.

### Query methods
- The `@Query` annotation lets you write SQL statements and expose them as DAO methods.
- Use these query methods to query data from your app's database or when you need to perform more complex insertions, updates, and deletions.

### Special return types
- Paginated queries with the Paging library
- Direct cursor access


# Database - P3
> To make the database class which holds the `AppDatabase`, it must satisfy the following conditions:
> - The class must be annotated with `@Database` annotation that includes an `entities` array that lists all of the data entities associated with database.
> - The class must be abstract that extends `RoomDatabase`.
> - For each DAO class associated with the database, the database class must define an abstract method that has **zero arguments** and returns an instance of the DAO class.
> 
> ```kotlin
> @Database(entities = [User::class], version = 1)
> abstract class AppDatabase : RoomDatabase() {
>     abstract fun userDao(): UserDao
> }
> ```

> **RoomDatabase Methods**
> 
> - `clearAllTables()` > Deletes all rows from all the tables that are registered to this database as `Database.entities`. This does NOT reset the auto-increment value generated by `PrimaryKey.autoGenerate`.
> - `close()` > Closes the database if it is already open.
> - `getInvalidationTracker()`: InvalidationTracker > The invalidation tracker for this database. You can use the invalidation tracker to get notified when certain tables in the database are modified.
> - `getOpenHelper()` > The SQLite open helper used by this database.
> - `getQueryExecutor()` > The Executor in use by this database for async queries.
> - `getTypeConverter()` > Gets the instance of the given Type Converter.
> - `inTransaction()` : Returns true if current thread is in a transaction.
> - `isOpen()` > True if database connection is open and initialized. When Room is configured with `RoomDatabase.Builder.setAutoCloseTimeout` the database is considered open even if internally the connection has been closed, unless manually closed.
> - `query()` > Convenience method to query the database with arguments.

# Annotations - P4

> `@Database` - Marks a class as a RoomDatabase.
> 
> Properties →
> - `autoMigrations` - List of AutoMigrations that can be performed on this Database.
> - `entities` - The list of entities included in the database. Each entity turns into a table in the database.
> - `exportSchema` - Whether the schema should be exported to the given folder when the `room.schemaLocation` argument is set. Defaults to true.
> - `version` - The database version.
> - `views` - The list of database views.

> `@Entity` - Marks a class as an entity. This class will have a mapping SQLite table in the database. Each entity must have at least 1 field annotated with `PrimaryKey`. You can also use primaryKeys attribute to define the primary key.
> 
> Properties → 
> - `tableName` - The table name in the SQLite database. If not set, defaults to the class name.
> - `primaryKeys` - The list of Primary Key column names.
> - `foreignKeys` - List of ForeignKey constraints on this entity.
> - `ignoredColumns` - The list of column names that should be ignored by Room. Normally, you can use Ignore, but this is useful for ignoring fields inherited from parents.

> `@PrimaryKey` - Marks a field in an Entity as the primary key. If you would like to define a composite primary key, you should use `Entity.primaryKeys` method. Each Entity must declare a primary key unless one of its super classes declares a primary key. If both an Entity and its super class defines a PrimaryKey, the child's PrimaryKey definition will override the parent's PrimaryKey.


> `@Query` - 
> - Marks a method in a Dao annotated class as a query method.
> - The value of the annotation includes the query that will be run when this method is called.
> - This query is verified at compile time by Room to ensure that it compiles fine against the database.
> - Room supports binding a list of parameters to the query. (max 999)
>   ```kotlin
>     @Query("SELECT * FROM song WHERE id IN(:songIds)")
>     public abstract fun findByIds(songIds: Array<Long>): List<Song>
>     ```


> `@ColumnInfo`- 
> 
> - collate: The collation sequence of the column. This is either UNSPECIFIED, BINARY, NOCASE, RTRIM, LOCALIZED or UNICODE. (Compare string)
> - defaultValue: The default value for this column.
> - index: Convenience method to index the field.
> - name: Name of the column in the database.
> - typeAffinity: The type affinity of the column. This is either UNDEFINED, TEXT, INTEGER, REAL, or BLOB.


> `@Insert`- Marks a method in a Dao annotated class as an insert method. All of the parameters of the Insert method must either be **classes annotated with Entity** or **collections/array of it**.
> 
> - entity: If the target entity is specified via entity then the parameters can be of arbitrary POJO types that will be interpreted as partial entities.
> - onConflict: What to do if a conflict happens. (REPLACE, ABORT, IGNORE, NONE)

> `@Update`- same properties as `@Insert`.

> `@Delete`- Marks a method in a Dao annotated class as a delete method.
> - entity: The target entity of the delete method. When this is declared, the delete method parameters are interpreted as partial entities when the type of the parameter differs from the target.







