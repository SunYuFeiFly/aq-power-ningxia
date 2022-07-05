package com.wangchen.utils;/**
 * @ProjectName: aq-power
 * @Package: com.wangchen.utils
 * @ClassName: CompanyUtils
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/6/2 14:25
 * @Version: 1.0
 */

import com.wangchen.entity.Branch;
import com.wangchen.entity.Company;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 全部公司信息储存
 * @auther cheng.zhang
 * @create 2020/6/2
 *
 */
@Slf4j
@Component
public class CompanyUtils {

    public static Map<String,Integer> companyMap = new HashMap<String,Integer>();

    public static Map<String,Integer> branchMap = new HashMap<String,Integer>();

    public static Lock lock = new ReentrantLock();

    public static Integer getCompanyMap(String companyName){

        try {
            lock.lock();
            if(companyMap.containsKey(companyName)){
                return companyMap.get(companyName);
            }
            return null;
        }catch (Exception e){
            log.error("获取公司信息失败");
            return null;
        }finally {
//            if(lock.tryLock()){
                lock.unlock();
//            }
        }
    }

    public static Integer getBranchMap(String branchName){

        try {
            lock.lock();
            if(branchMap.containsKey(branchName)){
                return branchMap.get(branchName);
            }
            return null;
        }catch (Exception e){
            log.error("获取部门信息失败");
            return null;
        }finally {
//            if(lock.tryLock()){
                lock.unlock();
//            }
        }
    }

    public static void setCompanyMap(List<Company> companyList){
        try {
            lock.lock();
            for(Company company:companyList){
                companyMap.put(company.getName(),company.getId());
            }
        }catch (Exception e){
            log.error("存放全部公司信息失败");
        }finally {
//            if(lock.tryLock()){
                lock.unlock();
//            }
        }
    }

    public static void setBranchMap(List<Branch> branchList){
        try {
            lock.lock();
            for(Branch branch:branchList){
                branchMap.put(branch.getName(),branch.getId());
            }
        }catch (Exception e){
            log.error("存放全部部门信息失败");
        }finally {
//            if(lock.tryLock()){
                lock.unlock();
//            }
        }
    }

}