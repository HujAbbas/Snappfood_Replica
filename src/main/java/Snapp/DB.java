package Snapp;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;

public class DB {
    static Connection connection = null;
    static Statement statement = null;
    static ResultSet resultSet = null;
    static private PreparedStatement preparedStatement = null;
    public static void main(String[] args) throws SQLException {
        init();
//        addaccount("abbas","mewmew","1234",true,2000);
//        System.out.println(usertaken("mae"));
    }
    public static void init()
    {

        try {
            // below two lines are used for connectivity.
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/db",
                    "root", "snappfood");
            statement = connection.createStatement();
//            preparedStatement = connection.prepareStatement("INSERT INTO db.users (name, username, password, isadmin,credit)\n" +
//                    "VALUES (?, ?, ?, ?,?);");
//            preparedStatement.setString(1, name);
//            preparedStatement.setString(2, username);
//            preparedStatement.setString(3, pass);
//            preparedStatement.setBoolean(4, isadmin);
//            preparedStatement.setLong(5, credit);
//            preparedStatement.executeUpdate();
        }
        catch (Exception exception) {
            System.out.println(exception);
        }
    }
    static void savefoods(ArrayList<Food> foods) throws SQLException {
        for(Food f:foods){
            preparedStatement = connection.prepareStatement("INSERT INTO db.food (id, name, restaurantid,type,price,discount,cookingtime,imagepath)\n" +
                    "VALUES (?, ?, ?,?,?,?,?,?);");
            preparedStatement.setInt(1, f.getId());
            preparedStatement.setString(2, f.getName());
            preparedStatement.setInt(3, f.getOwner().getId());
            preparedStatement.setString(4, f.getFoodType().toString());
            preparedStatement.setDouble(5, f.getPrice());
            preparedStatement.setDouble(6, f.getDiscount());
            preparedStatement.setLong(7, f.getCookingTime());
            preparedStatement.setString(8, f.getImageURL());
            preparedStatement.executeUpdate();
        }
    }
    static ArrayList<Food> loadfoods() throws SQLException, Food.InvalidPriceException, FoodType.UnknownType {
        statement = connection.createStatement();
        resultSet = statement.executeQuery("select id, name, restaurantid,type,price,discount,cookingtime,imagepath from db.food ;");
        ArrayList<Food> foods = new ArrayList<>();
        while (resultSet.next()){
            Food f = new Food(resultSet.getInt("id"),resultSet.getString("name"),resultSet.getLong("price"),FoodType.parse(resultSet.getString("type")),resultSet.getLong("cookingtime"),null);
            f.ownerid=resultSet.getInt("restaurantid");
            f.setImageURL(resultSet.getString("imagepath"));
            foods.add(f);
        }
        return foods;
    }
    static void saveRestaurants(ArrayList<Restaurant> restaurants) throws SQLException {
        for(Restaurant f:restaurants){
            preparedStatement = connection.prepareStatement("INSERT INTO db.restaurants (id, name, adminid,type,loc,imagepath)\n" +
                    "VALUES (?, ?, ?,?,?,?);");
            preparedStatement.setInt(1, f.getId());
            preparedStatement.setString(2, f.getName());
            preparedStatement.setInt(3, f.getOwner().id);
            preparedStatement.setString(4, f.getFoodtype().toString());
            preparedStatement.setInt(5, f.getLocation());
            preparedStatement.setString(6, f.getImageURL());
            preparedStatement.executeUpdate();
        }
    }
    static ArrayList<Restaurant> loadRestaurants() throws SQLException, FoodType.UnknownType, Restaurant.FoodTypeUnchangable {
        statement = connection.createStatement();
        resultSet = statement.executeQuery("select  id, name, adminid,type,loc,imagepath from db.restaurants ;");
        ArrayList<Restaurant> restaurants = new ArrayList<>();
        while (resultSet.next()){
            String type = resultSet.getString("type");
            ArrayList<FoodType> foodtype = new ArrayList<>();
            for(String s:type.substring(1, type.length() - 1).split(", ")){
                foodtype.add(FoodType.parse(s));
            }
            Restaurant f = new Restaurant(resultSet.getString("name"),resultSet.getInt("id"),foodtype.get(0) ,null,resultSet.getInt("loc"));
            f.ownerid = resultSet.getInt("adminid");
            f.setImageURL(resultSet.getString("imagepath"));
            f.addFoodtype(foodtype);
            for( Account c: Account.AccountList){
                if(c.getId()==f.getOwner().getId()){
                    ((Admin)c).addRestaurant(f);
                }
            }
            restaurants.add(f);
        }
        return restaurants;
    }

    static void saveAccounts(ArrayList<Account> accounts) throws SQLException {
        for(Account f:accounts){
            preparedStatement = connection.prepareStatement("INSERT INTO db.users (id, username, password,salt,isadmin,isdelivery,currency,location,answer,question)\n" +
                    "VALUES (?, ?, ?,?,?,?,?,?,?,?);");
            preparedStatement.setInt(1, f.getId());
            preparedStatement.setString(2, f.getName());
            preparedStatement.setString(3, new String(f.getHashedPassword()));
            preparedStatement.setString(4, new String(f.getSalt()));
            preparedStatement.setBoolean(5, f.isadmin);
            preparedStatement.setBoolean(6, f.isDelivery);
            preparedStatement.setInt(7, ((f.isIsadmin()||f.isDelivery())?0:((User)f).getCurrency()));
            preparedStatement.setInt(8, f.getLocation());
            preparedStatement.setString(9,f.getAnswer());
            preparedStatement.setString(10, f.getQuestion());
            preparedStatement.executeUpdate();
        }
    }

    static ArrayList<Account> loadAccounts() throws SQLException, FoodType.UnknownType,  Account.InvalidPasswordException, Account.InvalidUsernameException, NoSuchAlgorithmException, InvalidKeySpecException {
        statement = connection.createStatement();
        resultSet = statement.executeQuery("select id, username, password,salt,isadmin,isdelivery,currency,location,question,answer from db.users ;");
        ArrayList<Account> accounts = new ArrayList<>();
        while (resultSet.next()){
            Account f;
            if(resultSet.getBoolean("isadmin")){ //user is admin(doesnt matter here)
                f = new Admin();
                f.setName(resultSet.getString("username"));
                f.setId(resultSet.getInt("id"));
                f.setSalt(resultSet.getString("salt").getBytes());
                f.setHashedPassword(resultSet.getString("password").getBytes());
                f.setIsadmin(resultSet.getBoolean("isadmin"));
                f.setDelivery(resultSet.getBoolean("isdelivery"));
                f.setLocation(resultSet.getInt("location"));
                f.setAnswer(resultSet.getString("answer"));
                f.setQuestion(resultSet.getString("question"));
                for(Restaurant r: Restaurant.getRestaurantList()){
                    if(r.ownerid==f.id){
                        ((Admin)f).addRestaurant(r);
                        r.setOwner((Admin) f);
                    }
                }
            }
            else if(resultSet.getBoolean("isdelivery")){ //user is delivery(doesnt matter here)
                f = new Delivery();
                f.setName(resultSet.getString("username"));
                f.setId(resultSet.getInt("id"));
                f.setSalt(resultSet.getString("salt").getBytes());
                f.setHashedPassword(resultSet.getString("password").getBytes());
                f.setIsadmin(resultSet.getBoolean("isadmin"));
                f.setDelivery(resultSet.getBoolean("isdelivery"));
                f.setLocation(resultSet.getInt("location"));
                f.setAnswer(resultSet.getString("answer"));
                f.setQuestion(resultSet.getString("question"));
            }
            else{                                   // the point is giving currency
                f = new User();
                f.setName(resultSet.getString("username"));
                f.setId(resultSet.getInt("id"));
                f.setSalt(resultSet.getString("salt").getBytes());
                f.setHashedPassword(resultSet.getString("password").getBytes());
                f.setIsadmin(resultSet.getBoolean("isadmin"));
                f.setDelivery(resultSet.getBoolean("isdelivery"));
                f.setLocation(resultSet.getInt("location"));
                ((User) f).setCurrency(resultSet.getInt("currency"));
                f.setAnswer(resultSet.getString("answer"));
                f.setQuestion(resultSet.getString("question"));
            }
            accounts.add(f);
            Account.nextID = Math.max(Account.nextID,f.getId()+1);
        }
        return accounts;
    }
    static void saveOrders(ArrayList<Order> orders) throws SQLException {
        for(Order f:orders){
            int foodcount = f.getFoods().size();
            byte[] few = new byte[foodcount];
            for(int i=0;i<foodcount;++i){
                few[i] = (byte)f.getFoods().get(i).getId();
            }
            preparedStatement = connection.prepareStatement("INSERT INTO db.orders (id, customerid, restaurantid,foodcount,foods,state,cookingtime,cookingstarttime)\n" +
                    "VALUES (?, ?, ?,?,?,?,?,?);");
            preparedStatement.setInt(1, f.getId());
            preparedStatement.setInt(2, f.getCostomer().getId());
            preparedStatement.setInt(3, f.getRecipient().getId());
            preparedStatement.setInt(4, foodcount);
            preparedStatement.setString(5, new String(few));
            preparedStatement.setInt(6, f.getOrderState().ordinal());
            preparedStatement.setLong(7, f.getCookingTime());
            preparedStatement.setLong(8, f.getCookingStartTime());
            preparedStatement.executeUpdate();
        }
    }
    static ArrayList<Order> loadOrders() throws SQLException, FoodType.UnknownType, Restaurant.FoodTypeUnchangable, Food.InvalidFoodID {
        statement = connection.createStatement();
        resultSet = statement.executeQuery("select id, customerid, restaurantid,foodcount,foods,cookingtime,cookingstarttime,state from db.orders ;");
        ArrayList<Order> orders = new ArrayList<>();
        while (resultSet.next()){

            ArrayList<Food> foods = new ArrayList<>();
            byte[] ids = new byte[resultSet.getByte("foodcount")];
            for(byte id:ids){
                foods.add(Food.getFoodbyId(id));
            }
            Order f = new Order(resultSet.getInt("id") ,User.getUserByID(resultSet.getInt("customerid")),foods,Restaurant.getRestaurantByID(resultSet.getInt("restaurantid")));
            f.setOrderState(OrderState.values()[resultSet.getInt("state")]);
            f.setCookingTime(resultSet.getLong("cookingtime"));
            f.setCookingStartTime(resultSet.getLong("cookingstarttime"));
            orders.add(f);
            for(Account a:Account.AccountList){
                if(a.getId()==f.getCostomer().id) {
                    if(f.orderIsActive()) ((User) a).setActiveOrder(f);
                    else{
                        ((User) a).receivedOrders.add(f);
                    }
                }
            }
            for(Restaurant r:Restaurant.getRestaurantList()){
                if(r.getId()==f.getRecipient().getId()) r.addOrder(f);
            }
            Order.nextId = Math.max(Order.nextId,f.getId()+1);
        }
        return orders;
    }
    static void saveCooments(ArrayList<Comment> comments) throws SQLException {
        for(Comment f:comments){
            if (f.getFood() != null){
                preparedStatement = connection.prepareStatement("INSERT INTO db.comments (id, userid, foodid,message)\n" +
                        "VALUES (?, ?, ?,?);");
                preparedStatement.setInt(1, f.getID());
                preparedStatement.setInt(2, f.getCommenter().getId());
                preparedStatement.setInt(3, f.getFood().getId());
                preparedStatement.setString(4, f.getMessage());
                if (f.getUpper() != null) {
                    preparedStatement = connection.prepareStatement("INSERT INTO db.comments (upper)\n" +
                            "VALUES (?);");
                    preparedStatement.setInt(1, f.getUpper().getID());
                    preparedStatement.executeUpdate();
                }
                preparedStatement.executeUpdate();
            }
            if (f.getRestaurant() != null) {
                preparedStatement = connection.prepareStatement("INSERT INTO db.comments (id, userid, restaurantid,message)\n" +
                        "VALUES (?, ?, ?,?);");
                preparedStatement.setInt(1, f.getID());
                preparedStatement.setInt(2, f.getCommenter().getId());
                preparedStatement.setInt(3, f.getRestaurant().getId());
                preparedStatement.setString(4, f.getMessage());
                preparedStatement.executeUpdate();

                if (f.getUpper() != null) {
                    preparedStatement = connection.prepareStatement("INSERT INTO db.comments (upper)\n" +
                            "VALUES (?);");
                    preparedStatement.setInt(1, f.getUpper().getID());
                    preparedStatement.executeUpdate();
                }

            }
        }
    }

    static ArrayList<Comment> loadComments() throws SQLException, Food.InvalidFoodID, Comment.CommentDoesntExistException {
        statement = connection.createStatement();
        resultSet = statement.executeQuery("select id, userid, foodid,restaurantid,message,upper from db.comments ;");
        ArrayList<Comment> comments = new ArrayList<>();
        while (resultSet.next()){
            Comment f = new Comment();
            f.setCommenter(User.getUserByID(resultSet.getInt("userid")));
            f.setFood(Food.getFoodbyId(resultSet.getInt("foodid")));
            f.setID(resultSet.getInt("id"));
            f.setMessage(resultSet.getString("message"));
            if(resultSet.getInt("upper")!=-1)f.setUpper(Comment.getCommentById(resultSet.getInt("upper")));
            comments.add(f);
        }
        for(Comment c: comments){
            Comment.nextID=Math.max(Comment.nextID,c.getID()+1);
            if(c.getUpper()!=null) c.getUpper().setReply(c);
        }
        return comments;
    }

    static void saveCarts(ArrayList<Cart> carts) throws SQLException {
        for(Cart f:carts){
            if(f!=null){
                int count = f.getFoods().size();
                byte[] foo = new byte[count];
                for (int i = 0; i < count; ++i) {
                    foo[i] = (byte) f.getFoods().get(i).getId();
                }
                preparedStatement = connection.prepareStatement("INSERT INTO db.carts (userid, restaurantid, foods,foodscount)\n" +
                        "VALUES (?, ?, ?,?);");
                preparedStatement.setInt(1, f.getCostomer().getId());
                preparedStatement.setInt(2, f.getRecipient().getId());
                preparedStatement.setString(3, new String(foo));
                preparedStatement.setInt(4, count);
                preparedStatement.executeUpdate();
            }
        }
    }
    static void loadCarts() throws SQLException, Food.InvalidFoodID, Food.FoodIsDeactiveException {
        statement = connection.createStatement();
        resultSet = statement.executeQuery("select userid, restaurantid, foods,foodscount from db.carts ;");
        ArrayList<Cart> carts = new ArrayList<>();
        while (resultSet.next()){
            Cart f = new Cart(Restaurant.getRestaurantByID(resultSet.getInt("restaurantid")),User.getUserByID(resultSet.getInt("userid")));
            for(byte b :resultSet.getString("foods").getBytes() ){
                f.addFood(Food.getFoodbyId(b));
            }
            carts.add(f);
        }
        for(Cart c:carts){
            User.getUserByID(c.getCostomer().getId()).setCart(c);
        }
    }
    static void loadRatings() throws SQLException, Food.InvalidFoodID, Food.FoodIsDeactiveException, User.YouHaveAlreadyRated {
        statement = connection.createStatement();
        resultSet = statement.executeQuery("select userid, restaurantid,foodid,value from db.ratings ;");
        while (resultSet.next()) {
            int restid = resultSet.getInt("restaurantid");
            if(resultSet.wasNull()){
                User.getUserByID(resultSet.getInt("userid")).addRatingToFood(Food.getFoodbyId(resultSet.getInt("foodid")),resultSet.getDouble("value"));
            }
            else{
                User.getUserByID(resultSet.getInt("userid")).addRatingToRestaurant(Restaurant.getRestaurantByID(restid),resultSet.getDouble("value"));
            }

        }
    }
    static void saveFoodRatings(Food food) throws SQLException {
        for(Rating r:food.ratings){
                preparedStatement = connection.prepareStatement("INSERT INTO db.ratings (userid, foodid, value)\n" +
                        "VALUES (?, ?, ?);");
                preparedStatement.setInt(1, r.getUser().getId());
                preparedStatement.setInt(2, food.getId());
                preparedStatement.setDouble(3, r.getRating());
                preparedStatement.executeUpdate();
            }
        }
    static void saveRestaurantRatings(Restaurant restaurant) throws SQLException {
        for(Rating r:restaurant.ratings){
            preparedStatement = connection.prepareStatement("INSERT INTO db.ratings (userid, restaurantid, value)\n" +
                    "VALUES (?, ?, ?);");
            preparedStatement.setInt(1, r.getUser().getId());
            preparedStatement.setInt(2, restaurant.getId());
            preparedStatement.setDouble(3, r.getRating());
            preparedStatement.executeUpdate();
        }
    }

    static void clearEverything() throws SQLException {
        preparedStatement = connection.prepareStatement("truncate table db.food;");
        preparedStatement.executeUpdate();
        preparedStatement = connection.prepareStatement("truncate table db.users;");
        preparedStatement.executeUpdate();
        preparedStatement = connection.prepareStatement("truncate table db.restaurants;");
        preparedStatement.executeUpdate();
        preparedStatement = connection.prepareStatement("truncate table db.orders;");
        preparedStatement.executeUpdate();
        preparedStatement = connection.prepareStatement("truncate table db.comments;");
        preparedStatement.executeUpdate();
        preparedStatement = connection.prepareStatement("truncate table db.carts;");
        preparedStatement.executeUpdate();
        preparedStatement = connection.prepareStatement("truncate table db.ratings;");
        preparedStatement.executeUpdate();
    }
    static void close(){
            try {
                if (resultSet != null) {
                    resultSet.close();
                }

                if (statement != null) {
                    statement.close();
                }

                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {

            }
    }

} // class ends