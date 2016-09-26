package se.sics.isc4flink.models.poisson;
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
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.windowing.time.Time;
import se.sics.isc4flink.core.AnomalyResult;
import se.sics.isc4flink.core.KeyedAnomalyFlatMap;
import se.sics.isc4flink.core.WindowTimeExtractor;
import se.sics.isc4flink.history.History;
import se.sics.isc4flink.models.CountSumFold;

public class PoissonValueAnomaly<K,V> {

    private KeyedAnomalyFlatMap<K,PoissonModel> afm;

    public PoissonValueAnomaly(boolean addIfAnomaly, double anomalyLevel, History hist){
        this.afm = new KeyedAnomalyFlatMap<>(14d,new PoissonModel(hist), true);
    }

    public PoissonValueAnomaly(History hist){
        new PoissonFreqAnomaly(false,14d,hist);
    }

    public DataStream<Tuple2<K, AnomalyResult>> getAnomalySteam(DataStream<V> ds, KeySelector<V, K> keySelector, KeySelector<V,Double> valueSelector, Time window) {

        KeyedStream<V, K> keyedInput = ds
                .keyBy(keySelector);

        TypeInformation<Tuple2<K,Tuple4<Double,Double,Long,Long>>> resultType = (TypeInformation) new TupleTypeInfo<>(Tuple2.class,
                new TypeInformation[] {keyedInput.getKeyType(), new TupleTypeInfo(Tuple4.class,
                        BasicTypeInfo.DOUBLE_TYPE_INFO, BasicTypeInfo.DOUBLE_TYPE_INFO, BasicTypeInfo.LONG_TYPE_INFO,BasicTypeInfo.LONG_TYPE_INFO)});

        Tuple2<K,Tuple4<Double,Double,Long,Long>> init= new Tuple2<>(null,new Tuple4<>(0d,0d,0l,0l));
        KeyedStream<Tuple2<K,Tuple4<Double,Double,Long,Long>>, Tuple> kPreStream = keyedInput
                .timeWindow(window)
                .apply(init, new CountSumFold<>(keySelector,valueSelector, resultType),new WindowTimeExtractor(resultType))
                .keyBy(0);

        return kPreStream.flatMap(afm);
    }



}
