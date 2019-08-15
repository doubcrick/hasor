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
package net.hasor.core.binder;
import net.hasor.core.container.BindInfoContainer;
import net.hasor.core.container.ScopContainer;
import net.hasor.core.container.SpiCallerContainer;
import net.hasor.core.environment.StandardEnvironment;
import net.hasor.core.info.DefaultBindInfoProviderAdapter;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.mockito.Matchers.anyObject;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016-12-16
 */
public class AbstractBinderDataTest {
    protected Logger                                          logger = LoggerFactory.getLogger(getClass());
    protected Predicate<Class<?>>                             ignoreMatcher;
    protected AtomicReference<DefaultBindInfoProviderAdapter> reference;
    protected ApiBinderWrap                                   binder;

    //
    public void beforeTest() throws IOException {
        this.reference = new AtomicReference<>();
        //
        BindInfoContainer bindInfoContainer = PowerMockito.mock(BindInfoContainer.class);
        PowerMockito.when(bindInfoContainer.createInfoAdapter((Class<?>) anyObject())).thenAnswer((Answer<Object>) invocationOnMock -> {
            Class<Object> targetType = (Class<Object>) invocationOnMock.getArguments()[0];
            DefaultBindInfoProviderAdapter<Object> adapter = new DefaultBindInfoProviderAdapter<>(targetType);
            Predicate<Class<?>> defaultMatcher = (ignoreMatcher == null) ? (aClass -> false) : ignoreMatcher;
            if (defaultMatcher.test(targetType)) {
                return adapter;
            }
            reference.set(adapter);
            return reference.get();
        });
        //
        BindInfoBuilderFactory factory = PowerMockito.mock(BindInfoBuilderFactory.class);
        PowerMockito.when(factory.getBindInfoContainer()).thenReturn(bindInfoContainer);
        //
        SpiCallerContainer spiContainer = new SpiCallerContainer();
        ScopContainer scopFactory = new ScopContainer(spiContainer);
        scopFactory.init();
        PowerMockito.when(factory.getScopContainer()).thenReturn(scopFactory);
        this.binder = new ApiBinderWrap(new AbstractBinder(new StandardEnvironment(null)) {
            @Override
            protected BindInfoBuilderFactory containerFactory() {
                return factory;
            }
        });
    }
}