/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cobar.manager.jdbcmock.domain;

import java.sql.ResultSet;
import java.sql.SQLException;



public class SalesOrderImpl implements SalesOrder
{
    
    private String orderNumber;
    private String region;
    private double totalPrice;
    

    /* (non-Javadoc)
     * @see xmleasymock.demo.test.SalesOrder#getSalesOrderNumber()
     */
    @Override
    public String getOrderNumber()
    {
        return orderNumber;
    }

    /* (non-Javadoc)
     * @see xmleasymock.demo.test.SalesOrder#getSalesRepID()
     */
    @Override
    public String getRegion()
    {
        return region;
    }

    /* (non-Javadoc)
     * @see xmleasymock.demo.test.SalesOrder#getTotalPrice()
     */
    @Override
    public double getTotalPrice()
    {
        return totalPrice;
    }

    /* (non-Javadoc)
     * @see xmleasymock.demo.test.SalesOrder#setSalesOrderNumber(java.lang.String)
     */
    @Override
    public void setOrderNumber(String orderNumber)
    {
        this.orderNumber = orderNumber;
    }

    /* (non-Javadoc)
     * @see xmleasymock.demo.test.SalesOrder#setSalesRepID(java.lang.String)
     */
    @Override
    public void setRegion(String region)
    {
        this.region = region;
    }

    /* (non-Javadoc)
     * @see xmleasymock.demo.test.SalesOrder#setTotalPrice(double)
     */
    @Override
    public void setTotalPrice(double totalPrice)
    {
        this.totalPrice = totalPrice;
    }

    /* (non-Javadoc)
     * @see xmleasymock.demo.test.SalesOrder#loadDataFromDB(java.sql.ResultSet)
     */
    @Override
    public void loadDataFromDB(ResultSet resultSet) throws SQLException
    {
        orderNumber = resultSet.getString(1);
        region = resultSet.getString(2);
        totalPrice = resultSet.getDouble(3);
    }
    /* (non-Javadoc)
     * @see xmleasymock.demo.test.SalesOrder#getPriceLevel()
     */
    @Override
    public String getPriceLevel()
    {
        double totalPrice = this.getTotalPrice();
        double totalPoints = 0.0;
        
        if ("Africa".equalsIgnoreCase(this.getRegion()))
            totalPoints = totalPrice;
        else if ("Asia Pacific".equalsIgnoreCase(this.getRegion()))
            totalPoints = totalPrice * 0.9;
        else if ("Europe".equalsIgnoreCase(this.getRegion()))
            totalPoints = totalPrice * 0.85;
        else if ("America".equalsIgnoreCase(this.getRegion()))
            totalPoints = totalPrice * 0.8;
        else
            totalPoints = totalPrice * 0.75;
        
        if (totalPoints < 500)
            return "Level_A";
        else if (totalPoints < 1000)
            return "Level_B";
        else if (totalPoints < 2000)
            return "Level_C";
        else if (totalPoints < 4000)
            return "Level_D";
        else
            return "Level_E";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("orderNumber = "+orderNumber+"\n");
        sb.append("region = "+region+"\n");
        sb.append("totalPrice = "+totalPrice+"\n");
        sb.append("priceLevel = "+this.getPriceLevel()+"\n");
        return sb.toString();
    }
}

