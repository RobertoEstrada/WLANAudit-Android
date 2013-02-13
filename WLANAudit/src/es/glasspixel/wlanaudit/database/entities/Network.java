/*
 * Copyright (C) 2013 The WLANAudit project contributors.
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

package es.glasspixel.wlanaudit.database.entities;

import java.io.Serializable;
import java.util.List;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.Index;
import org.orman.mapper.annotation.PrimaryKey;

import es.glasspixel.wlanaudit.keyframework.IKeyCalculator;
import es.glasspixel.wlanaudit.keyframework.KeyCalculatorFactory;
import es.glasspixel.wlanaudit.keyframework.NetData;

/**
 * Maps to the saved networks table on the database
 */
@Entity
public class Network extends Model<Network> implements Serializable {
    
    private transient static final long serialVersionUID = 8043495355710840188L;
    @PrimaryKey(autoIncrement = true)
    public int id;
    public String mSSID;
    @Index(unique = true)
    public String mBSSID;
    public String mEncryption;
    public int mFrequency;
    public int mChannel;
    public double mLatitude;
    public double mLongitude;
    private transient List<String> mPossibleDefaultKeys;

    public Network() {
    }

    public List<String> getPossibleDefaultKeys() {
        if (mPossibleDefaultKeys == null) {
            IKeyCalculator keyCalculator = KeyCalculatorFactory.getKeyCalculator(new NetData(mSSID,
                    mBSSID));
            if (keyCalculator != null) {
                mPossibleDefaultKeys = keyCalculator.getKey(new NetData(mSSID, mBSSID));
            } else {
                return null;
            }
        }
        return mPossibleDefaultKeys;
    }
}
