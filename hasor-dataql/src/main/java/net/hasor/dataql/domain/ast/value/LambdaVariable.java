/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.dataql.domain.ast.value;
import net.hasor.dataql.Option;
import net.hasor.dataql.domain.ast.Variable;
import net.hasor.dataql.domain.ast.inst.InstSet;
import net.hasor.dataql.domain.compiler.CompilerStack;
import net.hasor.dataql.domain.compiler.InstQueue;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * lambda 函数对象
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-03-23
 */
public class LambdaVariable extends InstSet implements Variable {
    private List<String> paramList = new ArrayList<>();

    /** 添加入参 */
    public void addParam(String name) {
        if (this.paramList.contains(name)) {
            throw new java.lang.IllegalStateException(name + " param existing.");
        }
        this.paramList.add(name);
    }

    @Override
    public void doCompiler(InstQueue queue, CompilerStack stackTree) {
        //
        int size = this.paramList.size();
        int methodAddress = 0;
        stackTree.newFrame();
        {
            // .输出 lambda 到一个新的函数中
            InstQueue instQueue = queue.newMethodInst();
            methodAddress = instQueue.getName();
            //
            // .函数定义
            instQueue.inst(METHOD, size);
            //
            // .声明函数参数的变量位置
            for (String name : this.paramList) {
                int index = stackTree.push(name);//将变量名压栈，并返回栈中的位置
                instQueue.inst(LOCAL, index, name);  //为栈中某个位置的变量命名
            }
            // .函数体
            super.doCompiler(instQueue, stackTree);
        }
        stackTree.dropFrame();
        //
        // .指向函数的指针
        queue.inst(M_REF, methodAddress);
    }

    @Override
    public void doFormat(int depth, Option formatOption, Writer writer) {
        //
    }
}