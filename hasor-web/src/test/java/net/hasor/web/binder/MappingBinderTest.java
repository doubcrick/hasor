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
package net.hasor.web.binder;
import net.hasor.core.AppContext;
import net.hasor.core.BindInfo;
import net.hasor.core.provider.InstanceProvider;
import net.hasor.test.actions.basic.AnnoGetAction;
import net.hasor.test.actions.basic.BasicAction;
import net.hasor.test.actions.mapping.MappingServlet;
import net.hasor.test.actions.servlet.SimpleServlet;
import net.hasor.test.filters.SimpleFilter;
import net.hasor.test.filters.SimpleInvokerFilter;
import net.hasor.web.AbstractTest;
import net.hasor.web.ServletVersion;
import net.hasor.web.WebApiBinder;
import net.hasor.web.annotation.MappingTo;
import net.hasor.web.startup.RuntimeFilter;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @version : 2016-12-16
 * @author 赵永春 (zyc@hasor.net)
 */
public class MappingBinderTest extends AbstractTest {
    @Test
    public void binder_0() {
        AppContext appContext1 = buildWebAppContext(apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).setEncodingCharacter("UTF-8-TTT", "UTF-8-AAA");
        }, servlet25("/"), LoadModule.Web);
        assert "UTF-8-TTT".equals(appContext1.findBindingBean(RuntimeFilter.HTTP_REQUEST_ENCODING_KEY, String.class));
        assert "UTF-8-AAA".equals(appContext1.findBindingBean(RuntimeFilter.HTTP_RESPONSE_ENCODING_KEY, String.class));
        //
        buildWebAppContext(apiBinder -> {
            assert apiBinder.tryCast(WebApiBinder.class).getServletVersion() == ServletVersion.V2_4;
        }, servlet24("/"), LoadModule.Web);
        buildWebAppContext(apiBinder -> {
            assert apiBinder.tryCast(WebApiBinder.class).getServletVersion() == ServletVersion.V2_5;
        }, servlet25("/"), LoadModule.Web);
        buildWebAppContext(apiBinder -> {
            assert apiBinder.tryCast(WebApiBinder.class).getServletVersion() == ServletVersion.V3_0;
        }, servlet30("/"), LoadModule.Web);
        buildWebAppContext(apiBinder -> {
            assert apiBinder.tryCast(WebApiBinder.class).getServletVersion() == ServletVersion.V3_1;
        }, servlet31("/"), LoadModule.Web);
    }

    @Test
    public void mappingTest_1() {
        final String[] urls = new String[] { "/abc.do", "/def.do" };
        final AnnoGetAction testCallerAction = new AnnoGetAction();
        final Supplier<AnnoGetAction> testCallerActionProvider = InstanceProvider.of(testCallerAction);
        //
        AppContext appContext = buildWebAppContext(apiBinder -> {
            BindInfo<AnnoGetAction> filterBindInfo1 = apiBinder.bindType(AnnoGetAction.class).uniqueName().asEagerSingleton().toInfo();
            BindInfo<AnnoGetAction> filterBindInfo2 = apiBinder.bindType(AnnoGetAction.class).uniqueName().toInfo();
            //
            apiBinder.tryCast(WebApiBinder.class).mappingTo(urls).with(testCallerAction);          // 1
            apiBinder.tryCast(WebApiBinder.class).mappingTo(urls).with(testCallerActionProvider);  // 2
            apiBinder.tryCast(WebApiBinder.class).mappingTo(urls).with(AnnoGetAction.class);       // 3
            apiBinder.tryCast(WebApiBinder.class).mappingTo(urls).with(filterBindInfo1);           // 4
            apiBinder.tryCast(WebApiBinder.class).mappingTo(urls).with(filterBindInfo2);           // 5
        }, servlet30("/"), LoadModule.Web);
        //
        List<MappingDef> definitions = appContext.findBindingBean(MappingDef.class);
        assert definitions.size() == 10;
        for (int i = 0; i < 10; i++) {
            assert definitions.get(i).getClass() == MappingDef.class;
        }
        //
        assert "/abc.do".equals(definitions.get(0).getMappingTo());
        assert "/abc.do".equals(definitions.get(2).getMappingTo());
        assert "/abc.do".equals(definitions.get(4).getMappingTo());
        assert "/abc.do".equals(definitions.get(6).getMappingTo());
        assert "/abc.do".equals(definitions.get(8).getMappingTo());
        //
        assert "/def.do".equals(definitions.get(1).getMappingTo());
        assert "/def.do".equals(definitions.get(3).getMappingTo());
        assert "/def.do".equals(definitions.get(5).getMappingTo());
        assert "/def.do".equals(definitions.get(7).getMappingTo());
        assert "/def.do".equals(definitions.get(9).getMappingTo());
        //
        Object invoke1_1 = appContext.getInstance(definitions.get(0).getTargetType());  // 1
        Object invoke1_2 = appContext.getInstance(definitions.get(1).getTargetType());  // 1
        Object invoke2_1 = appContext.getInstance(definitions.get(2).getTargetType());  // 2
        Object invoke2_2 = appContext.getInstance(definitions.get(3).getTargetType());  // 2
        Object invoke3_1 = appContext.getInstance(definitions.get(4).getTargetType());  // 3
        Object invoke3_2 = appContext.getInstance(definitions.get(5).getTargetType());  // 3
        Object invoke4_1 = appContext.getInstance(definitions.get(6).getTargetType());  // 4
        Object invoke4_2 = appContext.getInstance(definitions.get(7).getTargetType());  // 4
        Object invoke5_1 = appContext.getInstance(definitions.get(8).getTargetType());  // 5
        Object invoke5_2 = appContext.getInstance(definitions.get(9).getTargetType());  // 5
        //
        assert invoke1_1 == invoke1_2;
        assert invoke2_1 == invoke2_2;
        assert invoke3_1 != invoke3_2;
        assert invoke4_1 == invoke4_2;
        assert invoke5_1 != invoke5_2;
    }

    @Test
    public void mappingTest_2() {
        final String[] urls = new String[] { "/abc.do", "/def.do" };
        final AnnoGetAction testCallerAction = new AnnoGetAction();
        final Supplier<AnnoGetAction> testCallerActionProvider = InstanceProvider.of(testCallerAction);
        //
        AppContext appContext = buildWebAppContext(apiBinder -> {
            BindInfo<AnnoGetAction> filterBindInfo1 = apiBinder.bindType(AnnoGetAction.class).uniqueName().asEagerSingleton().toInfo();
            BindInfo<AnnoGetAction> filterBindInfo2 = apiBinder.bindType(AnnoGetAction.class).uniqueName().toInfo();
            //
            apiBinder.tryCast(WebApiBinder.class).mappingTo(urls).with(1, testCallerAction);          // 1
            apiBinder.tryCast(WebApiBinder.class).mappingTo(urls).with(2, testCallerActionProvider);  // 2
            apiBinder.tryCast(WebApiBinder.class).mappingTo(urls).with(3, AnnoGetAction.class);       // 3
            apiBinder.tryCast(WebApiBinder.class).mappingTo(urls).with(4, filterBindInfo1);           // 4
            apiBinder.tryCast(WebApiBinder.class).mappingTo(urls).with(5, filterBindInfo2);           // 5
        }, servlet30("/"), LoadModule.Web);
        //
        List<MappingDef> definitions = appContext.findBindingBean(MappingDef.class);
        assert definitions.size() == 10;
        for (int i = 0; i < 10; i++) {
            assert definitions.get(i).getClass() == MappingDef.class;
        }
        //
        assert "/abc.do".equals(definitions.get(0).getMappingTo());
        assert "/abc.do".equals(definitions.get(2).getMappingTo());
        assert "/abc.do".equals(definitions.get(4).getMappingTo());
        assert "/abc.do".equals(definitions.get(6).getMappingTo());
        assert "/abc.do".equals(definitions.get(8).getMappingTo());
        assert definitions.get(0).getIndex() == 1;
        assert definitions.get(2).getIndex() == 2;
        assert definitions.get(4).getIndex() == 3;
        assert definitions.get(6).getIndex() == 4;
        assert definitions.get(8).getIndex() == 5;
        //
        assert "/def.do".equals(definitions.get(1).getMappingTo());
        assert "/def.do".equals(definitions.get(3).getMappingTo());
        assert "/def.do".equals(definitions.get(5).getMappingTo());
        assert "/def.do".equals(definitions.get(7).getMappingTo());
        assert "/def.do".equals(definitions.get(9).getMappingTo());
        assert definitions.get(1).getIndex() == 1;
        assert definitions.get(3).getIndex() == 2;
        assert definitions.get(5).getIndex() == 3;
        assert definitions.get(7).getIndex() == 4;
        assert definitions.get(9).getIndex() == 5;
        //
        Object invoke1_1 = appContext.getInstance(definitions.get(0).getTargetType());  // 1
        Object invoke1_2 = appContext.getInstance(definitions.get(1).getTargetType());  // 1
        Object invoke2_1 = appContext.getInstance(definitions.get(2).getTargetType());  // 2
        Object invoke2_2 = appContext.getInstance(definitions.get(3).getTargetType());  // 2
        Object invoke3_1 = appContext.getInstance(definitions.get(4).getTargetType());  // 3
        Object invoke3_2 = appContext.getInstance(definitions.get(5).getTargetType());  // 3
        Object invoke4_1 = appContext.getInstance(definitions.get(6).getTargetType());  // 4
        Object invoke4_2 = appContext.getInstance(definitions.get(7).getTargetType());  // 4
        Object invoke5_1 = appContext.getInstance(definitions.get(8).getTargetType());  // 5
        Object invoke5_2 = appContext.getInstance(definitions.get(9).getTargetType());  // 5
        //
        assert invoke1_1 == invoke1_2;
        assert invoke2_1 == invoke2_2;
        assert invoke3_1 != invoke3_2;
        assert invoke4_1 == invoke4_2;
        assert invoke5_1 != invoke5_2;
    }

    @Test
    public void mappingTest_3() {
        buildWebAppContext(apiBinder -> {
            try {
                apiBinder.tryCast(WebApiBinder.class).mappingTo(new String[0]).with(AnnoGetAction.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("mappingTo patterns is empty.");
            }
            try {
                apiBinder.tryCast(WebApiBinder.class).mappingTo(new String[] { "", null }).with(AnnoGetAction.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("mappingTo patterns is empty.");
            }
        }, servlet30("/"), LoadModule.Web);
    }

    @Test
    public void mappingTest_4() {
        AppContext appContext = buildWebAppContext(apiBinder -> {
            try {
                apiBinder.tryCast(WebApiBinder.class).loadMappingTo(BasicAction.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().endsWith(" must be configure @MappingTo");
            }
            try {
                apiBinder.tryCast(WebApiBinder.class).loadMappingTo(AppContext.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().endsWith(" must be normal Bean");
            }
            //
            Set<Class<?>> classSet = apiBinder.findClass(MappingTo.class, "net.hasor.test.actions.mapping.*");
            assert classSet.size() == 2;
            apiBinder.tryCast(WebApiBinder.class).loadMappingTo(classSet);
        }, servlet30("/"), LoadModule.Web);
        //
        List<MappingDef> definitions = appContext.findBindingBean(MappingDef.class);
        assert definitions.size() == 2;
        //
        Set<String> mappingToSet = new HashSet<>();
        mappingToSet.add(definitions.get(0).getMappingTo());
        mappingToSet.add(definitions.get(1).getMappingTo());
        //
        assert mappingToSet.contains("/mappingto_a.do");
        assert mappingToSet.contains("/mappingto_b.do");
    }

    @Test
    public void mappingTest_5() {
        buildWebAppContext(apiBinder -> {
            try {
                apiBinder.tryCast(WebApiBinder.class).mappingTo(new String[0]).with(AnnoGetAction.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("mappingTo patterns is empty.");
            }
            try {
                apiBinder.tryCast(WebApiBinder.class).mappingTo(new String[] { "", null }).with(AnnoGetAction.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("mappingTo patterns is empty.");
            }
        }, servlet30("/"), LoadModule.Web);
    }

    @Test
    public void filterTest_1() throws Throwable {
        final String[] urls = new String[] { "/abc.do", "/def.do" };
        final SimpleInvokerFilter testCallerFilter = new SimpleInvokerFilter();
        final Supplier<SimpleInvokerFilter> testCallerFilterProvider = InstanceProvider.of(testCallerFilter);
        //
        AppContext appContext = buildWebAppContext(apiBinder -> {
            BindInfo<SimpleInvokerFilter> filterBindInfo1 = apiBinder.bindType(SimpleInvokerFilter.class).uniqueName().asEagerSingleton().toInfo();
            BindInfo<SimpleInvokerFilter> filterBindInfo2 = apiBinder.bindType(SimpleInvokerFilter.class).uniqueName().toInfo();
            //
            apiBinder.tryCast(WebApiBinder.class).filter(urls).through(1, testCallerFilter);           // 1
            apiBinder.tryCast(WebApiBinder.class).filter(urls).through(2, testCallerFilterProvider);   // 2
            apiBinder.tryCast(WebApiBinder.class).filter(urls).through(3, SimpleInvokerFilter.class);     // 3
            apiBinder.tryCast(WebApiBinder.class).filter(urls).through(4, filterBindInfo1);            // 4
            apiBinder.tryCast(WebApiBinder.class).filter(urls).through(5, filterBindInfo2);            // 5
        }, servlet30("/"), LoadModule.Web);
        //
        List<FilterDef> definitions = appContext.findBindingBean(FilterDef.class);
        assert definitions.size() == 10;
        for (int i = 0; i < 10; i++) {
            OneConfig oneConfig = new OneConfig("", () -> appContext);
            oneConfig.put("arg_string1", "abc");
            oneConfig.put("arg_string2", "def");
            definitions.get(i).init(oneConfig);
        }
        //
        assert "/abc.do".equals(definitions.get(0).getMatcher().getPattern());
        assert "/abc.do".equals(definitions.get(2).getMatcher().getPattern());
        assert "/abc.do".equals(definitions.get(4).getMatcher().getPattern());
        assert "/abc.do".equals(definitions.get(6).getMatcher().getPattern());
        assert "/abc.do".equals(definitions.get(8).getMatcher().getPattern());
        assert definitions.get(0).getIndex() == 1;
        assert definitions.get(2).getIndex() == 2;
        assert definitions.get(4).getIndex() == 3;
        assert definitions.get(6).getIndex() == 4;
        assert definitions.get(8).getIndex() == 5;
        //
        assert "/def.do".equals(definitions.get(1).getMatcher().getPattern());
        assert "/def.do".equals(definitions.get(3).getMatcher().getPattern());
        assert "/def.do".equals(definitions.get(5).getMatcher().getPattern());
        assert "/def.do".equals(definitions.get(7).getMatcher().getPattern());
        assert "/def.do".equals(definitions.get(9).getMatcher().getPattern());
        assert definitions.get(1).getIndex() == 1;
        assert definitions.get(3).getIndex() == 2;
        assert definitions.get(5).getIndex() == 3;
        assert definitions.get(7).getIndex() == 4;
        assert definitions.get(9).getIndex() == 5;
        //
        Object invoke0 = appContext.getInstance(definitions.get(0).getTargetType());     // 1
        Object invoke2_1 = appContext.getInstance(definitions.get(2).getTargetType());   // 2
        Object invoke2_2 = appContext.getInstance(definitions.get(2).getTargetType());   // 2
        Object invoke4_1 = appContext.getInstance(definitions.get(4).getTargetType());   // 3
        Object invoke4_2 = appContext.getInstance(definitions.get(4).getTargetType());   // 3
        Object invoke6_1 = appContext.getInstance(definitions.get(6).getTargetType());   // 4
        Object invoke6_2 = appContext.getInstance(definitions.get(6).getTargetType());   // 4
        Object invoke8_1 = appContext.getInstance(definitions.get(8).getTargetType());   // 5
        Object invoke8_2 = appContext.getInstance(definitions.get(8).getTargetType());   // 5
        //
        assert invoke0 == testCallerFilter;
        assert invoke2_1 == testCallerFilter;
        assert invoke2_1 == invoke2_2;
        assert invoke4_1 == invoke4_2;
        assert invoke6_1 == invoke6_2;
        assert invoke8_1 == invoke8_2;
        //
        for (int i = 0; i < 10; i++) {
            assert "abc".equals(definitions.get(i).getInitParams().getInitParameter("arg_string1"));
            assert "def".equals(definitions.get(i).getInitParams().getInitParameter("arg_string2"));
        }
    }

    @Test
    public void filterTest_2() throws Throwable {
        final String[] urls = new String[] { "/abc.do", "/def.do" };
        final SimpleFilter simpleFilter = new SimpleFilter();
        final SimpleInvokerFilter invokerFilter = new SimpleInvokerFilter();
        //
        AppContext appContext = buildWebAppContext(apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).filter(urls).through(invokerFilter);          // 1
            apiBinder.tryCast(WebApiBinder.class).filterRegex(urls).through(invokerFilter);     // 2
            apiBinder.tryCast(WebApiBinder.class).jeeFilter(urls).through(simpleFilter);        // 3
            apiBinder.tryCast(WebApiBinder.class).jeeFilterRegex(urls).through(simpleFilter);   // 4
        }, servlet30("/"), LoadModule.Web);
        //
        List<FilterDef> definitions = appContext.findBindingBean(FilterDef.class);
        assert definitions.size() == 8;
        //
        assert "/abc.do".equals(definitions.get(0).getMatcher().getPattern());
        assert "/abc.do".equals(definitions.get(2).getMatcher().getPattern());
        assert "/abc.do".equals(definitions.get(4).getMatcher().getPattern());
        assert "/abc.do".equals(definitions.get(6).getMatcher().getPattern());
        assert definitions.get(0) instanceof FilterDef;
        assert definitions.get(2) instanceof FilterDef;
        assert definitions.get(4) instanceof FilterDef;
        assert definitions.get(6) instanceof FilterDef;
        //
        assert "/def.do".equals(definitions.get(1).getMatcher().getPattern());
        assert "/def.do".equals(definitions.get(3).getMatcher().getPattern());
        assert "/def.do".equals(definitions.get(5).getMatcher().getPattern());
        assert "/def.do".equals(definitions.get(7).getMatcher().getPattern());
        assert definitions.get(1) instanceof FilterDef;
        assert definitions.get(3) instanceof FilterDef;
        assert definitions.get(5) instanceof FilterDef;
        assert definitions.get(7) instanceof FilterDef;
        //
        for (int i = 0; i < 8; i++) {
            definitions.get(i).init(new OneConfig("", () -> appContext));
        }
        //
        Object invoke0 = appContext.getInstance(definitions.get(0).getTargetType());     // 1
        Object invoke2 = appContext.getInstance(definitions.get(2).getTargetType());     // 2
        Object invoke4_1 = appContext.getInstance(definitions.get(4).getTargetType());   // 3
        Object invoke4_2 = appContext.getInstance(definitions.get(4).getTargetType());   // 3
        Object invoke6_1 = appContext.getInstance(definitions.get(6).getTargetType());   // 4
        Object invoke6_2 = appContext.getInstance(definitions.get(6).getTargetType());   // 4
        //
        assert invoke0 == invoke2;
        assert invoke4_1 == invoke4_2;
        assert invoke6_1 == invoke6_2;
        //
        assert invoke4_1 instanceof J2eeFilterAsFilter;
        assert invoke6_1 instanceof J2eeFilterAsFilter;
    }

    @Test
    public void filterTest_3() {
        buildWebAppContext(apiBinder -> {
            try {
                apiBinder.tryCast(WebApiBinder.class).filter(new String[0]).through(SimpleInvokerFilter.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("Filter patterns is empty.");
            }
            try {
                apiBinder.tryCast(WebApiBinder.class).filter(new String[] { "", null }).through(SimpleInvokerFilter.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("Filter patterns is empty.");
            }
            //
            //
            try {
                apiBinder.tryCast(WebApiBinder.class).filterRegex(new String[0]).through(SimpleInvokerFilter.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("Filter patterns is empty.");
            }
            try {
                apiBinder.tryCast(WebApiBinder.class).filterRegex(new String[] { "", null }).through(SimpleInvokerFilter.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("Filter patterns is empty.");
            }
            //
            //
            try {
                apiBinder.tryCast(WebApiBinder.class).jeeFilter(new String[0]).through(SimpleFilter.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("Filter patterns is empty.");
            }
            try {
                apiBinder.tryCast(WebApiBinder.class).jeeFilter(new String[] { "", null }).through(SimpleFilter.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("Filter patterns is empty.");
            }
            //
            //
            try {
                apiBinder.tryCast(WebApiBinder.class).jeeFilterRegex(new String[0]).through(SimpleFilter.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("Filter patterns is empty.");
            }
            try {
                apiBinder.tryCast(WebApiBinder.class).jeeFilterRegex(new String[] { "", null }).through(SimpleFilter.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("Filter patterns is empty.");
            }
        }, servlet30("/"), LoadModule.Web);
    }

    @Test
    public void servletTest_1() {
        final String[] urls = new String[] { "/abc.do", "/def.do" };
        final SimpleServlet testCallerServlet = new SimpleServlet();
        final Supplier<SimpleServlet> testCallerServletProvider = InstanceProvider.of(testCallerServlet);
        //
        AppContext appContext = buildWebAppContext(apiBinder -> {
            BindInfo<SimpleServlet> filterBindInfo1 = apiBinder.bindType(SimpleServlet.class).uniqueName().asEagerSingleton().toInfo();
            OneConfig oneConfig = new OneConfig("", apiBinder.getProvider(AppContext.class));
            oneConfig.put("arg_string1", "abc");
            oneConfig.put("arg_string2", "def");
            //
            apiBinder.tryCast(WebApiBinder.class).jeeServlet(urls).with(1, testCallerServlet, oneConfig);          // 1
            apiBinder.tryCast(WebApiBinder.class).jeeServlet(urls).with(2, testCallerServletProvider, oneConfig);  // 2
            apiBinder.tryCast(WebApiBinder.class).jeeServlet(urls).with(3, MappingServlet.class, oneConfig);       // 3
            apiBinder.tryCast(WebApiBinder.class).jeeServlet(urls).with(4, filterBindInfo1, oneConfig);            // 4
        }, servlet30("/"), LoadModule.Web);
        //
        List<MappingDef> definitions = appContext.findBindingBean(MappingDef.class);
        assert definitions.size() == 8;
        //
        assert "/abc.do".equals(definitions.get(0).getMappingTo());
        assert "/abc.do".equals(definitions.get(2).getMappingTo());
        assert "/abc.do".equals(definitions.get(4).getMappingTo());
        assert "/abc.do".equals(definitions.get(6).getMappingTo());
        assert definitions.get(0).getIndex() == 1;
        assert definitions.get(2).getIndex() == 2;
        assert definitions.get(4).getIndex() == 3;
        assert definitions.get(6).getIndex() == 4;
        assert definitions.get(0) instanceof MappingDef;
        assert definitions.get(2) instanceof MappingDef;
        assert definitions.get(4) instanceof MappingDef;
        assert definitions.get(6) instanceof MappingDef;
        //
        assert "/def.do".equals(definitions.get(1).getMappingTo());
        assert "/def.do".equals(definitions.get(3).getMappingTo());
        assert "/def.do".equals(definitions.get(5).getMappingTo());
        assert "/def.do".equals(definitions.get(7).getMappingTo());
        assert definitions.get(1).getIndex() == 1;
        assert definitions.get(3).getIndex() == 2;
        assert definitions.get(5).getIndex() == 3;
        assert definitions.get(7).getIndex() == 4;
        assert definitions.get(1) instanceof MappingDef;
        assert definitions.get(3) instanceof MappingDef;
        assert definitions.get(5) instanceof MappingDef;
        assert definitions.get(7) instanceof MappingDef;
        //
        Object invoke1_1 = appContext.getInstance(definitions.get(0).getTargetType());  // 1
        Object invoke1_2 = appContext.getInstance(definitions.get(1).getTargetType());  // 1
        Object invoke2_1 = appContext.getInstance(definitions.get(2).getTargetType());  // 2
        Object invoke2_2 = appContext.getInstance(definitions.get(3).getTargetType());  // 2
        Object invoke3_1 = appContext.getInstance(definitions.get(4).getTargetType());  // 3
        Object invoke3_2 = appContext.getInstance(definitions.get(5).getTargetType());  // 3
        Object invoke4_1 = appContext.getInstance(definitions.get(6).getTargetType());  // 4
        Object invoke4_2 = appContext.getInstance(definitions.get(7).getTargetType());  // 4
        //
        assert invoke1_1 instanceof J2eeServletAsMapping;
        assert invoke1_2 instanceof J2eeServletAsMapping;
        assert invoke2_1 instanceof J2eeServletAsMapping;
        assert invoke2_2 instanceof J2eeServletAsMapping;
        assert invoke3_1 instanceof J2eeServletAsMapping;
        assert invoke3_2 instanceof J2eeServletAsMapping;
        assert invoke4_1 instanceof J2eeServletAsMapping;
        assert invoke4_2 instanceof J2eeServletAsMapping;
        //
        assert ((J2eeServletAsMapping) invoke1_1).targetServlet.get() == ((J2eeServletAsMapping) invoke1_2).targetServlet.get();
        assert ((J2eeServletAsMapping) invoke2_1).targetServlet.get() == ((J2eeServletAsMapping) invoke2_2).targetServlet.get();
        assert ((J2eeServletAsMapping) invoke3_1).targetServlet.get() == ((J2eeServletAsMapping) invoke3_2).targetServlet.get();
        assert ((J2eeServletAsMapping) invoke4_1).targetServlet.get() == ((J2eeServletAsMapping) invoke4_2).targetServlet.get();
        //
        assert !testCallerServlet.isInit();// init 过程在 Caller 创建 J2eeServletAsMapping 的时候每次去执行一次 init。
    }

    @Test
    public void servletTest_2() {
        buildWebAppContext(apiBinder -> {
            try {
                apiBinder.tryCast(WebApiBinder.class).jeeServlet(new String[0]).with(SimpleServlet.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("Servlet patterns is empty.");
            }
            try {
                apiBinder.tryCast(WebApiBinder.class).jeeServlet(new String[] { "", null }).with(SimpleServlet.class);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("Servlet patterns is empty.");
            }
        }, servlet30("/"), LoadModule.Web);
    }
    //    //
    //    @Test
    //    public void renderTest1() throws Throwable {
    //        final TestRenderEngine testRenderEngine = new TestRenderEngine();
    //        final Supplier<TestRenderEngine> testRenderEngineProvider = InstanceProvider.of(testRenderEngine);
    //        //
    //        AppContext appContext = hasor.build((WebModule) apiBinder -> {
    //            BindInfo<TestRenderEngine> engineBindInfo1 = apiBinder.bindType(TestRenderEngine.class).asEagerSingleton().toInfo();
    //            BindInfo<TestRenderEngine> engineBindInfo2 = apiBinder.bindType(TestRenderEngine.class).toInfo();
    //            //
    //            apiBinder.tryCast(WebApiBinder.class).addRender("htm1").toInstance(testRenderEngine);           // 1
    //            apiBinder.tryCast(WebApiBinder.class).addRender("htm2").toProvider(testRenderEngineProvider);   // 2
    //            apiBinder.tryCast(WebApiBinder.class).addRender("htm3").to(TestRenderEngine.class);             // 3
    //            apiBinder.tryCast(WebApiBinder.class).addRender("htm4").bindToInfo(engineBindInfo1);            // 4
    //            apiBinder.tryCast(WebApiBinder.class).addRender("htm5").bindToInfo(engineBindInfo2);            // 5
    //            try {
    //                apiBinder.tryCast(WebApiBinder.class).addRender("htm5").bindToInfo(engineBindInfo2);        // duplicate
    //                assert false;
    //            } catch (IllegalStateException e) {
    //                assert e.getMessage().startsWith("duplicate bind -> bindName 'htm5'");
    //            }
    //        });
    //        //
    //        List<RenderDefinition> definitions = appContext.findBindingBean(RenderDefinition.class);
    //        assert definitions.size() == 5;
    //        for (int i = 0; i < 5; i++) {
    //            assert definitions.get(i).getClass() == RenderDefinition.class;
    //        }
    //        //
    //        Object invoke1 = definitions.get(0).newEngine(appContext);     // 1
    //        Object invoke2 = definitions.get(1).newEngine(appContext);     // 2
    //        Object invoke3_1 = definitions.get(2).newEngine(appContext);   // 3
    //        Object invoke3_2 = definitions.get(2).newEngine(appContext);   // 3
    //        Object invoke4_1 = definitions.get(3).newEngine(appContext);   // 4
    //        Object invoke4_2 = definitions.get(3).newEngine(appContext);   // 4
    //        Object invoke5_1 = definitions.get(4).newEngine(appContext);   // 5
    //        Object invoke5_2 = definitions.get(4).newEngine(appContext);   // 5
    //        //
    //        assert invoke1 == invoke2;
    //        assert invoke3_1 != invoke3_2;
    //        assert invoke4_1 == invoke4_2;
    //        assert invoke5_1 != invoke5_2;
    //    }
    //    //
    //    @Test
    //    public void renderTest2() throws Throwable {
    //        final TestRenderEngine testRenderEngine = new TestRenderEngine();
    //        final Supplier<TestRenderEngine> testRenderEngineProvider = InstanceProvider.of(testRenderEngine);
    //        //
    //        AppContext appContext = hasor.build((WebModule) apiBinder -> {
    //            BindInfo<TestRenderEngine> engineBindInfo1 = apiBinder.bindType(TestRenderEngine.class).asEagerSingleton().toInfo();
    //            BindInfo<TestRenderEngine> engineBindInfo2 = apiBinder.bindType(TestRenderEngine.class).toInfo();
    //            //
    //            apiBinder.tryCast(WebApiBinder.class).addRender("htm1").toInstance(testRenderEngine);           // 1
    //            apiBinder.tryCast(WebApiBinder.class).addRender("htm2").toProvider(testRenderEngineProvider);   // 2
    //            apiBinder.tryCast(WebApiBinder.class).addRender("htm3").to(TestRenderEngine.class);             // 3
    //            apiBinder.tryCast(WebApiBinder.class).addRender("htm4").bindToInfo(engineBindInfo1);            // 4
    //            apiBinder.tryCast(WebApiBinder.class).addRender("htm5").bindToInfo(engineBindInfo2);            // 5
    //        });
    //        //
    //        List<RenderDefinition> definitions = appContext.findBindingBean(RenderDefinition.class);
    //        assert definitions.size() == 5;
    //        for (int i = 0; i < 5; i++) {
    //            assert definitions.get(i).getClass() == RenderDefinition.class;
    //        }
    //        //
    //        Object invoke1 = definitions.get(0).newEngine(appContext);     // 1
    //        Object invoke2 = definitions.get(1).newEngine(appContext);     // 2
    //        Object invoke3_1 = definitions.get(2).newEngine(appContext);   // 3
    //        Object invoke3_2 = definitions.get(2).newEngine(appContext);   // 3
    //        Object invoke4_1 = definitions.get(3).newEngine(appContext);   // 4
    //        Object invoke4_2 = definitions.get(3).newEngine(appContext);   // 4
    //        Object invoke5_1 = definitions.get(4).newEngine(appContext);   // 5
    //        Object invoke5_2 = definitions.get(4).newEngine(appContext);   // 5
    //        //
    //        assert invoke1 == invoke2;
    //        assert invoke3_1 != invoke3_2;
    //        assert invoke4_1 == invoke4_2;
    //        assert invoke5_1 != invoke5_2;
    //    }
    //    //
    //    @Test
    //    public void loadRenderTest1() throws Throwable {
    //        //
    //        AppContext appContext = hasor.build((WebModule) apiBinder -> {
    //            try {
    //                apiBinder.tryCast(WebApiBinder.class).loadRender(BasicAction.class);
    //                assert false;
    //            } catch (Exception e) {
    //                assert e.getMessage().endsWith(" must be configure @Render");
    //            }
    //            try {
    //                apiBinder.tryCast(WebApiBinder.class).loadRender(AppContext.class);
    //                assert false;
    //            } catch (Exception e) {
    //                assert e.getMessage().endsWith(" must be normal Bean");
    //            }
    //            //
    //            try {
    //                apiBinder.tryCast(WebApiBinder.class).loadRender(ErrorRenderEngine.class);
    //                assert false;
    //            } catch (Exception e) {
    //                assert e.getMessage().endsWith(" must be implements RenderEngine.");
    //            }
    //            //
    //            Set<Class<?>> classSet = apiBinder.findClass(Render.class, "net.hasor.test.beans.beans");
    //            assert classSet.size() == 2;
    //            classSet.remove(ErrorRenderEngine.class); // remove Error
    //            apiBinder.tryCast(WebApiBinder.class).loadRender(classSet);
    //        });
    //        //
    //        List<RenderDefinition> definitions = appContext.findBindingBean(RenderDefinition.class);
    //        assert definitions.size() == 1;
    //        //
    //        Set<String> suffixSet = new HashSet<>();
    //        suffixSet.add(definitions.get(0).getRenderInfo().name());
    //        //
    //        assert suffixSet.size() == 1;
    //        assert suffixSet.contains("jspx");
    //    }
}