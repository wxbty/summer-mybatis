package ink.zfei.mybatis.jdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonMapper {

//    public List<Person> selectById(long id) {
//
//        Connection conn = null;
//        ResultSet resultSet = null;
//        PreparedStatement stmt = null;
//        try {
//            conn = DriverManager.getConnection("jdbc:mysql://118.190.155.151:3306/demo", "root", "123456");
//            String sql = "select * from device where id =  ?";
//            stmt = conn.prepareStatement(sql);
//            stmt.setLong(1, id);
//            resultSet = stmt.executeQuery();
//            List<Person> result = new ArrayList<>();
//            while (resultSet.next()) {
//                Person person = new Person();
//                person.setId(id);
//                person.setName(resultSet.getString("name"));
//                person.setStatus(resultSet.getInt("status"));
//                result.add(person);
//            }
//            return result;
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                resultSet.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return null;
//    }

}
