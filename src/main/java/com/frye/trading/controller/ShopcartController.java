
package com.frye.trading.controller;

import com.frye.trading.pojo.model.Customer;
import com.frye.trading.pojo.model.Shopcart;
import com.frye.trading.pojo.dto.Commodity;
import com.frye.trading.service.CommodityService;
import com.frye.trading.service.ShopcartService;
import com.frye.trading.utils.DataJsonUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class ShopcartController {

    @Autowired
    ShopcartService shopcartService;
    @Autowired
    CommodityService commodityService;

    @RequestMapping(value = "/op/addtoCart", method = RequestMethod.POST)
    @ResponseBody
    public String addToCart(@RequestBody String commodityId) {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        Customer customer = (Customer) session.getAttribute("customer");
        DataJsonUtils dataJsonUtils = new DataJsonUtils();
        if (shopcartService.addShopcart(customer.getCustomerId(),commodityId) < 0) {
            dataJsonUtils.setCode(100);
            dataJsonUtils.setMsg("add to cart error!");
        }else {
            dataJsonUtils.setCode(200);
            dataJsonUtils.setMsg("add to cart successfully!");
        }
        return dataJsonUtils.toString();
    }

    @RequestMapping("/op/getShopcartGoods")
    @ResponseBody
    public String getShopcartGoods() {
        DataJsonUtils dataJsonUtils = new DataJsonUtils();
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        Customer customer = (Customer) session.getAttribute("customer");
        String CustomerId = customer.getCustomerId();
        System.out.println(customer);

        List<Shopcart> shopcarts = shopcartService.getCommodityByCustomerId(CustomerId);
        List<Commodity> commodities = new LinkedList<>();
        for (Shopcart shopcart : shopcarts) {
            if (shopcart.getState().equals("invalid")) {
                Commodity invalidCommodity = new Commodity();
                invalidCommodity.setCommodityId(shopcart.getCommodityId());
                invalidCommodity.setImage("/image/invalid.jpg");
                commodities.add(invalidCommodity);
            } else {
                commodities.add(commodityService.getCommodityById(shopcart.getCommodityId()));
            }
        }
        int count = commodities.size();
        if(count > 0 ){
            dataJsonUtils.setCode(200);
            dataJsonUtils.setMsg("get data successfully");
            dataJsonUtils.setCount(count);
            dataJsonUtils.setData(commodities);
        }
        else{
            dataJsonUtils.setCode(0);
            dataJsonUtils.setMsg("get data failed");
        }
        return dataJsonUtils.toString();
    }

    @RequestMapping("/op/deleteShopcartGoods")
    @ResponseBody
    public int deleteShopcartGoods(@RequestBody String commodityId){
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        Customer customer = (Customer) session.getAttribute("customer");
        String CustomerId = customer.getCustomerId();

        return shopcartService.deleteShopcart(CustomerId,commodityId);
    }

    @RequestMapping( "/mall/order/{commodityId}")
    public String toOrderPage(@PathVariable("commodityId") String commodityId, Model model) {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        Customer buyer = (Customer) session.getAttribute("customer");
        String buyerId = buyer.getCustomerId();
        model.addAttribute("buyerId",buyerId);
        Commodity commodity = commodityService.getCommodityById(commodityId);
        String sellerId = commodity.getCustomerId();
        model.addAttribute("sellerId",sellerId);
        model.addAttribute("commodityId",commodityId);
        return "/mall/order";
    }
}
