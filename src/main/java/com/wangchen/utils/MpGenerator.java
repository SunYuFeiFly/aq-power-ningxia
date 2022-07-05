package com.wangchen.utils;//package com.wangchen;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

/**
 * Created by Administrator on 2019/6/3.
 */

public class MpGenerator {

    public static void main(String[] args) {
        AutoGenerator mpg = new AutoGenerator();
        // 选择 freemarker 引擎，默认 Velocity
//        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        gc.setAuthor("zhaoliutao");
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath + "/src/main/java/");
        gc.setFileOverride(false);// 是否覆盖同名文件，默认是false
        gc.setIdType(IdType.AUTO);//主键策略
        gc.setServiceName("%sService");//生成的service接口名是否有I
        gc.setActiveRecord(true);// 不需要ActiveRecord特性的请改为false
        gc.setEnableCache(false);// XML 二级缓存
        gc.setBaseResultMap(true);// XML ResultMap
        gc.setBaseColumnList(false);// XML 种生成基础列

        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setDbType(DbType.MYSQL);
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("tower");
        dsc.setPassword("tietalaskdfotu9867lkhsdvOPIUvdssv");
        dsc.setUrl("jdbc:mysql://192.168.1.83:3306/tower?characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&serverTimezone=Asia/Shanghai");
        mpg.setDataSource(dsc);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming((NamingStrategy.underline_to_camel));//下划线到驼峰的命名
        strategy.setTablePrefix(new String[] { "aq" });// 此处可以修改为您的表前缀
        strategy.setEntityLombokModel(true);
        strategy.setInclude(new String[] {"aq_company_rank"}); // 需要生成的表
        mpg.setStrategy(strategy);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.wangchen");//设置包名的parent
//        pc.setXml("com/wangchen/mapper");//设置xml文件的目录
        mpg.setPackageInfo(pc);

        // 执行生成
        mpg.execute();


    }
}


