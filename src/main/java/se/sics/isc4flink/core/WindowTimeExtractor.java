package se.sics.isc4flink.core;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class WindowTimeExtractor<K> implements WindowFunction<Tuple2<K,Tuple4<Double,Double,Long,Long>>,Tuple2<K,Tuple4<Double,Double,Long,Long>>,K,TimeWindow> ,ResultTypeQueryable<Tuple2<K,Tuple4<Double,Double,Long,Long>>> {
    private transient TypeInformation<Tuple2<K,Tuple4<Double,Double,Long,Long>>> resultType;

    public WindowTimeExtractor (TypeInformation<Tuple2<K, Tuple4<Double, Double,Long,Long>>> resultType){
        this.resultType = resultType;
    }

    @Override
    public void apply(K key, TimeWindow timeWindow, Iterable<Tuple2<K, Tuple4<Double, Double,Long,Long>>> iterable, Collector<Tuple2<K, Tuple4<Double, Double, Long, Long>>> collector) throws Exception {
        Tuple2<K,Tuple4<Double,Double,Long,Long>> out = iterable.iterator().next();
        out.f1.f2 = timeWindow.getStart();
        out.f1.f3 = timeWindow.getEnd();
        collector.collect(out);
    }

    @Override
    public TypeInformation<Tuple2<K, Tuple4<Double, Double,Long,Long>>> getProducedType() {
        return resultType;
    }
}
