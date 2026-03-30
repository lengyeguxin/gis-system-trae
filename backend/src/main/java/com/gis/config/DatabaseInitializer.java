package com.gis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Value("${app.load-test-data:false}")
    private boolean loadTestData;

    @Value("${app.load-division-data:false}")
    private boolean loadDivisionData;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("开始数据库初始化...");
        
        try {
            Connection connection = dataSource.getConnection();
            
            ClassPathResource schemaResource = new ClassPathResource("schema.sql");
            EncodedResource encodedSchema = new EncodedResource(schemaResource, StandardCharsets.UTF_8);
            ScriptUtils.executeSqlScript(connection, encodedSchema);
            System.out.println("schema.sql 执行成功");
            
            if (loadDivisionData) {
                ClassPathResource divisionResource = new ClassPathResource("division_data_full.sql");
                EncodedResource encodedDivision = new EncodedResource(divisionResource, StandardCharsets.UTF_8);
                ScriptUtils.executeSqlScript(connection, encodedDivision);
                System.out.println("division_data_full.sql 执行成功");
            }
            
            if (loadTestData) {
                ClassPathResource testResource = new ClassPathResource("test.sql");
                EncodedResource encodedTest = new EncodedResource(testResource, StandardCharsets.UTF_8);
                ScriptUtils.executeSqlScript(connection, encodedTest);
                System.out.println("test.sql 执行成功");
            }
            
            connection.close();
            System.out.println("数据库初始化完成！");
        } catch (Exception e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
