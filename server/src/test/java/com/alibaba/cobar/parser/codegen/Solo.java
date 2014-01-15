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
 * (created at 2011-6-2)
 */
package com.alibaba.cobar.parser.codegen;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class Solo {

    /**
     * @param args
     */
    public static void main(String[] args) {
        I i = new I();
        A b = new B();
        // A a = new A();

        b.accept(i);

    }

}

class A {
    void accept(I i) {
        System.out.println("A");
        i.visit(this);
    }
}

class B extends A {
    @Override
    void accept(I i) {
        System.out.println("B");
        i.visit(this);
    }
}

class I {
    void visit(A a) {
        System.out.println(" visit a");
    }

    void visit(B b) {
        System.out.println(" visit b");
    }
}
