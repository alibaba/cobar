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
/**
 * (created at 2011-8-5)
 */
package com.alibaba.cobar.route;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.route.hint.CobarHint;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class CobarHintTest extends TestCase {

    public void testHint1() throws Exception {
        String sql = "  /*!cobar: $dataNodeId =2.1, $table='offer'*/ select * ";
        CobarHint hint = CobarHint.parserCobarHint(sql, 2);
        Assert.assertEquals(" select * ", hint.getOutputSql());
        Assert.assertEquals("OFFER", hint.getTable());
        Assert.assertEquals(1, hint.getDataNodes().size());
        Assert.assertEquals(new Pair<Integer, Integer>(2, 1), hint.getDataNodes().get(0));

        sql = "  /*!cobar: $dataNodeId=0.0, $table='offer'*/ select * ";
        hint = CobarHint.parserCobarHint(sql, 0);
        Assert.assertEquals(" select * ", hint.getOutputSql());
        Assert.assertEquals("OFFER", hint.getTable());
        Assert.assertEquals(1, hint.getDataNodes().size());
        Assert.assertEquals(new Pair<Integer, Integer>(0, 0), hint.getDataNodes().get(0));

        sql = "  /*!cobar: $dataNodeId=0, $table='offer'*/ select * ";
        hint = CobarHint.parserCobarHint(sql, 0);
        Assert.assertEquals(" select * ", hint.getOutputSql());
        Assert.assertEquals("OFFER", hint.getTable());
        Assert.assertEquals(1, hint.getDataNodes().size());
        Assert.assertEquals(new Pair<Integer, Integer>(0, null), hint.getDataNodes().get(0));

        sql = "/*!cobar: $dataNodeId   = [ 1,2,5.2]  , $table =  'offer'   */ select * ";
        hint = CobarHint.parserCobarHint(sql, 0);
        Assert.assertEquals(" select * ", hint.getOutputSql());
        Assert.assertEquals("OFFER", hint.getTable());
        Assert.assertEquals(3, hint.getDataNodes().size());

        sql = "/*!cobar: $partitionOperand=( 'member_id' = 'm1'), $table='offer'*/ select * ";
        hint = CobarHint.parserCobarHint(sql, 0);
        Assert.assertEquals(" select * ", hint.getOutputSql());
        Assert.assertEquals("OFFER", hint.getTable());
        Pair<String[], Object[][]> pair = hint.getPartitionOperand();
        Assert.assertEquals(1, pair.getKey().length);
        Assert.assertEquals("MEMBER_ID", pair.getKey()[0]);
        Assert.assertEquals(1, pair.getValue().length);
        Assert.assertEquals(1, pair.getValue()[0].length);
        Assert.assertEquals("m1", pair.getValue()[0][0]);
        Assert.assertNull(hint.getDataNodes());

        sql = "/*!cobar:$partitionOperand =   ( 'member_id' = ['m1'  ,   'm2' ]  ), $table='offer'  , $replica=  2*/ select * ";
        hint = CobarHint.parserCobarHint(sql, 0);
        Assert.assertEquals(" select * ", hint.getOutputSql());
        Assert.assertEquals("OFFER", hint.getTable());
        Assert.assertEquals(2, hint.getReplica());
        pair = hint.getPartitionOperand();
        Assert.assertEquals(1, pair.getKey().length);
        Assert.assertEquals("MEMBER_ID", pair.getKey()[0]);
        Assert.assertEquals(2, pair.getValue().length);
        Assert.assertEquals(1, pair.getValue()[0].length);
        Assert.assertEquals("m1", pair.getValue()[0][0]);
        Assert.assertEquals("m2", pair.getValue()[1][0]);
        Assert.assertNull(hint.getDataNodes());

        sql = "/*!cobar:$partitionOperand=('member_id'=['m1', 'm2']),$table='offer',$replica=2*/ select * ";
        hint = CobarHint.parserCobarHint(sql, 0);
        Assert.assertEquals(" select * ", hint.getOutputSql());
        Assert.assertEquals("OFFER", hint.getTable());
        Assert.assertEquals(2, hint.getReplica());
        pair = hint.getPartitionOperand();
        Assert.assertEquals(1, pair.getKey().length);
        Assert.assertEquals("MEMBER_ID", pair.getKey()[0]);
        Assert.assertEquals(2, pair.getValue().length);
        Assert.assertEquals(1, pair.getValue()[0].length);
        Assert.assertEquals("m1", pair.getValue()[0][0]);
        Assert.assertEquals("m2", pair.getValue()[1][0]);
        Assert.assertNull(hint.getDataNodes());

        sql = "/*!cobar:$partitionOperand = ( ['offer_id','group_id'] = [123,'3c']), $table='offer'*/ select * ";
        hint = CobarHint.parserCobarHint(sql, 0);
        Assert.assertEquals(" select * ", hint.getOutputSql());
        Assert.assertEquals("OFFER", hint.getTable());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, hint.getReplica());
        pair = hint.getPartitionOperand();
        Assert.assertEquals(2, pair.getKey().length);
        Assert.assertEquals("OFFER_ID", pair.getKey()[0]);
        Assert.assertEquals("GROUP_ID", pair.getKey()[1]);
        Assert.assertEquals(1, pair.getValue().length);
        Assert.assertEquals(2, pair.getValue()[0].length);
        Assert.assertEquals(123L, pair.getValue()[0][0]);
        Assert.assertEquals("3c", pair.getValue()[0][1]);
        Assert.assertNull(hint.getDataNodes());

        sql = "/*!cobar:$partitionOperand=(['offer_id' , 'group_iD' ]=[ 123 , '3c' ]) ,$table = 'offer'*/ select * ";
        hint = CobarHint.parserCobarHint(sql, 0);
        Assert.assertEquals(" select * ", hint.getOutputSql());
        Assert.assertEquals("OFFER", hint.getTable());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, hint.getReplica());
        pair = hint.getPartitionOperand();
        Assert.assertEquals(2, pair.getKey().length);
        Assert.assertEquals("OFFER_ID", pair.getKey()[0]);
        Assert.assertEquals("GROUP_ID", pair.getKey()[1]);
        Assert.assertEquals(1, pair.getValue().length);
        Assert.assertEquals(2, pair.getValue()[0].length);
        Assert.assertEquals(123L, pair.getValue()[0][0]);
        Assert.assertEquals("3c", pair.getValue()[0][1]);
        Assert.assertNull(hint.getDataNodes());

        sql = "/*!cobar:$partitionOperand=(['offer_id','group_id']=[123,'3c']),$table='offer'*/ select * ";
        hint = CobarHint.parserCobarHint(sql, 0);
        Assert.assertEquals(" select * ", hint.getOutputSql());
        Assert.assertEquals("OFFER", hint.getTable());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, hint.getReplica());
        pair = hint.getPartitionOperand();
        Assert.assertEquals(2, pair.getKey().length);
        Assert.assertEquals("OFFER_ID", pair.getKey()[0]);
        Assert.assertEquals("GROUP_ID", pair.getKey()[1]);
        Assert.assertEquals(1, pair.getValue().length);
        Assert.assertEquals(2, pair.getValue()[0].length);
        Assert.assertEquals(123L, pair.getValue()[0][0]);
        Assert.assertEquals("3c", pair.getValue()[0][1]);
        Assert.assertNull(hint.getDataNodes());

        sql = "/*!cobar:$partitionOperand=(['offer_id','group_id']=[[123,'3c'],[234,'food']]), $table='offer'*/ select * ";
        hint = CobarHint.parserCobarHint(sql, 0);
        Assert.assertEquals(" select * ", hint.getOutputSql());
        Assert.assertEquals("OFFER", hint.getTable());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, hint.getReplica());
        pair = hint.getPartitionOperand();
        Assert.assertEquals(2, pair.getKey().length);
        Assert.assertEquals("OFFER_ID", pair.getKey()[0]);
        Assert.assertEquals("GROUP_ID", pair.getKey()[1]);
        Assert.assertEquals(2, pair.getValue().length);
        Assert.assertEquals(2, pair.getValue()[0].length);
        Assert.assertEquals(2, pair.getValue()[1].length);
        Assert.assertEquals(123L, pair.getValue()[0][0]);
        Assert.assertEquals("3c", pair.getValue()[0][1]);
        Assert.assertEquals(234L, pair.getValue()[1][0]);
        Assert.assertEquals("food", pair.getValue()[1][1]);
        Assert.assertNull(hint.getDataNodes());

        sql = "/*!cobar:$partitionOperand= ( [ 'ofFER_id','groUp_id' ]= [ [123,'3c'],[ 234,'food']]  ), $table='offer'*/select * ";
        hint = CobarHint.parserCobarHint(sql, 0);
        Assert.assertEquals("select * ", hint.getOutputSql());
        Assert.assertEquals("OFFER", hint.getTable());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, hint.getReplica());
        pair = hint.getPartitionOperand();
        Assert.assertEquals(2, pair.getKey().length);
        Assert.assertEquals("OFFER_ID", pair.getKey()[0]);
        Assert.assertEquals("GROUP_ID", pair.getKey()[1]);
        Assert.assertEquals(2, pair.getValue().length);
        Assert.assertEquals(2, pair.getValue()[0].length);
        Assert.assertEquals(2, pair.getValue()[1].length);
        Assert.assertEquals(123L, pair.getValue()[0][0]);
        Assert.assertEquals("3c", pair.getValue()[0][1]);
        Assert.assertEquals(234L, pair.getValue()[1][0]);
        Assert.assertEquals("food", pair.getValue()[1][1]);
        Assert.assertNull(hint.getDataNodes());

        sql = "/*!cobar:$partitionOperand=(['offer_id']=[123,234]), $table='offer'*/ select * ";
        hint = CobarHint.parserCobarHint(sql, 0);
        Assert.assertEquals(" select * ", hint.getOutputSql());
        Assert.assertEquals("OFFER", hint.getTable());
        Assert.assertEquals((int) RouteResultsetNode.DEFAULT_REPLICA_INDEX, hint.getReplica());
        pair = hint.getPartitionOperand();
        Assert.assertEquals(1, pair.getKey().length);
        Assert.assertEquals("OFFER_ID", pair.getKey()[0]);
        Assert.assertEquals(2, pair.getValue().length);
        Assert.assertEquals(1, pair.getValue()[0].length);
        Assert.assertEquals(1, pair.getValue()[1].length);
        Assert.assertEquals(123L, pair.getValue()[0][0]);
        Assert.assertEquals(234L, pair.getValue()[1][0]);
        Assert.assertNull(hint.getDataNodes());
    }

}
