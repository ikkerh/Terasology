/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.config.flexible;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.terasology.engine.SimpleUri;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class FlexibleConfigImplTest {
    private static final SimpleUri KEY_NON_EXISTENT = new SimpleUri("engine-tests:TestSettingX");

    public static class Get {
        private FlexibleConfig config;

        @Before
        public void setUp() throws Exception {
            config = new FlexibleConfigImpl("");
        }

        @Test
        public void testGet() throws Exception {
            SimpleUri id1 = new SimpleUri("engine-tests:TestSetting1");
            SimpleUri id2 = new SimpleUri("engine-tests:TestSetting2");

            config.getSettingBuilder(id1, Integer.class)
                    .defaultValue(0)
                    .publish();

            config.getSettingBuilder(id2, Double.class)
                    .defaultValue(0.0)
                    .publish();

            Setting<Integer> retrievedSetting1 = config.get(id1);
            Setting<Double> retrievedSetting2 = config.get(id2);

            // We need the references to be equal
            assertEquals(retrievedSetting1, retrievedSetting1);
            assertEquals(retrievedSetting2, retrievedSetting2);
        }
    }

    public static class Contains {
        private FlexibleConfig config;

        @Before
        public void setUp() throws Exception {
            config = new FlexibleConfigImpl("");
        }

        @Test
        public void testContains() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            config.getSettingBuilder(id, Integer.class)
                    .defaultValue(0)
                    .publish();

            assertTrue(config.contains(id));
        }

        @Test
        public void testNotContains() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            config.getSettingBuilder(id, Integer.class)
                    .defaultValue(0)
                    .publish();

            assertFalse(config.contains(KEY_NON_EXISTENT));
        }
    }

    public static class Add {
        private FlexibleConfig config;

        @Before
        public void setUp() throws Exception {
            config = new FlexibleConfigImpl("");
        }

        @Test
        public void testAdd() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            assertTrue(config.getSettingBuilder(id, Integer.class)
                    .defaultValue(0)
                    .publish());
        }

        @Test
        public void testAddExisting() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            assertTrue(config.getSettingBuilder(id, Integer.class)
                    .defaultValue(0)
                    .publish());
            assertFalse(config.getSettingBuilder(id, Integer.class)
                    .defaultValue(0)
                    .publish());
        }
    }

    public static class Remove {
        private FlexibleConfig config;

        @Before
        public void setUp() throws Exception {
            config = new FlexibleConfigImpl("");
        }

        @Test
        public void testRemove() throws Exception {
            SimpleUri id1 = new SimpleUri("engine-tests:TestSetting1");
            SimpleUri id2 = new SimpleUri("engine-tests:TestSetting2");

            config.getSettingBuilder(id1, Integer.class)
                    .defaultValue(0)
                    .publish();
            config.getSettingBuilder(id2, Integer.class)
                    .defaultValue(0)
                    .publish();

            assertTrue(config.remove(id1));
            assertTrue(config.remove(id2));
        }

        @Test
        public void testNonexistentRemove() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            config.getSettingBuilder(id, Integer.class)
                    .defaultValue(0)
                    .publish();

            assertFalse(config.remove(KEY_NON_EXISTENT));
        }

        @Test
        public void testSubscribedRemove() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            config.getSettingBuilder(id, Integer.class)
                    .defaultValue(0)
                    .publish();

            Setting setting = config.get(id);

            setting.subscribe(propertyChangeEvent -> {
            });

            assertFalse(config.remove(id));
        }
    }

    public static class JsonStorage {
        private static final String CONFIG_AS_JSON = "{\n" +
                "  \"description\": \"\",\n" +
                "  \"engine-tests:TestSetting3\": {\n" +
                "    \"a\": 31,\n" +
                "    \"b\": \"string\",\n" +
                "    \"c\": \"A2\"\n" +
                "  },\n" +
                "  \"engine-tests:TestSetting1\": \"A3\",\n" +
                "  \"engine-tests:TestSetting2\": 21.0\n" +
                "}";


        private static final TestEnum TESTENUM_SETTING_VALUE = TestEnum.A3;
        private static final double DOUBLE_SETTING_VALUE = 21.0;
        private static final TestClass TESTCLASS_SETTING_VALUE = new TestClass();

        private FlexibleConfig config;

        private Setting<TestEnum> testEnumSetting;
        private Setting<Double> doubleSetting;
        private Setting<TestClass> testClassSetting;

        private void setupSettings() {
            SimpleUri testEnumSettingId = new SimpleUri("engine-tests:TestSetting1");

            config.getSettingBuilder(testEnumSettingId, TestEnum.class)
                    .defaultValue(TestEnum.A1)
                    .publish();

            testEnumSetting = config.get(testEnumSettingId);

            SimpleUri doubleSettingId = new SimpleUri("engine-tests:TestSetting2");

            config.getSettingBuilder(doubleSettingId, Double.class)
                    .defaultValue(30.0)
                    .publish();

            doubleSetting = config.get(doubleSettingId);

            SimpleUri testClassSettingId = new SimpleUri("engine-tests:TestSetting3");

            config.getSettingBuilder(testClassSettingId, TestClass.class)
                    .defaultValue(new TestClass(101))
                    .publish();

            testClassSetting = config.get(testClassSettingId);
        }

        @Before
        public void setUp() throws Exception {
            config = new FlexibleConfigImpl("");
        }

        @Test
        public void testSave() throws Exception {
            setupSettings();

            testEnumSetting.setValue(TESTENUM_SETTING_VALUE);
            doubleSetting.setValue(DOUBLE_SETTING_VALUE);
            testClassSetting.setValue(TESTCLASS_SETTING_VALUE);

            Writer writer = new StringWriter();
            config.save(writer);

            assertEquals(CONFIG_AS_JSON, writer.toString());
        }

        @Test
        public void testLoad() throws Exception {
            Reader reader = new StringReader(CONFIG_AS_JSON);
            config.load(reader);

            setupSettings();

            assertEquals(TESTENUM_SETTING_VALUE, testEnumSetting.getValue());
            assertEquals(DOUBLE_SETTING_VALUE, doubleSetting.getValue(), 0.00000001);
            assertEquals(TESTCLASS_SETTING_VALUE, testClassSetting.getValue());
        }

        private enum TestEnum {
            A1(1), A2(2), A3(3);

            private int i;

            TestEnum(int i) {
                this.i = i;
            }

            public int getI() {
                return i;
            }
        }

        private static class TestClass {
            int a;
            String b;

            TestEnum c;

            public TestClass() {
                this(31);
            }

            public TestClass(int a) {
                this.a = a;
                b = "string";
                c = TestEnum.A2;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                TestClass testClass = (TestClass) o;

                if (a != testClass.a) return false;
                if (b != null ? !b.equals(testClass.b) : testClass.b != null) return false;
                return c == testClass.c;
            }
        }

    }
}
