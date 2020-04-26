import { hmac } from 'js-sha256';
var aesjs = require('aes-js');
import { decode as atob, encode as btoa } from 'base-64'

var DAY_MILISECONDS = 24 * 60 * 60 * 1000;
var EPOCH_MILISECONDS = 60 * 60 * 1000;
var EPHEMERAL_MILISECONDS = 5 * 60 * 1000;
var SAVE_BACK_DAYS_AMOUNT = 14;
var SAVE_FORWARD_DAYS = 14;

function numToString(num, size) {
    var str = "";
    var i;
    for (i = 0; i < size; i += 1) {
        str += String.fromCharCode((num >> (8 * i)) & 0xff);
    }
    return str;
}

function byteToString(num) {
    return numToString(num, 1);
}

function intToString(num) {
    return numToString(num, 4);
}

function pad16(str) {
    return str.padEnd(16, String.fromCharCode(0));
}

function stringToArr(s) {
    var arr = new Uint8Array(s.length);
    var i;
    for (i = 0; i < s.length; i += 1) {
        arr[i] = s.charCodeAt(i);
    }
    return arr;
}

function toHex(str) {
    var hex = "";
    var i;
    for (i = 0; i < str.length; i += 1) {
        hex += str.charCodeAt(i).toString(16).padStart(2, "0");
    }
    return hex;
}

function unhex(hex) {
    var str = "";
    var i;
    for (i = 0; i < hex.length; i += 2) {
        str += String.fromCharCode(parseInt(hex.substr(i, 2), 16));
    }
    return str;
}

function xor_strings(s1, s2) {
    var str = "";
    var i;
    for (i = 0; i < s1.length; i += 1) {
        str += String.fromCharCode(s1.charCodeAt(i) ^ s2.charCodeAt(i));
    }
    return str;
}

function hmac16(key, data) {
    var keyArr = stringToArr(key);
    var dataArr = stringToArr(data);
    return unhex(hmac.create(keyArr).update(dataArr).hex()).substr(0, 16);
}

function encrypt(key, data) {
    var keyBytes = aesjs.utils.hex.toBytes(toHex(key));
    var dataBytes = aesjs.utils.hex.toBytes(toHex(data));
    var aesEcb = new aesjs.ModeOfOperation.ecb(keyBytes);
    var resultBytes = aesEcb.encrypt(dataBytes);
    return unhex(aesjs.utils.hex.fromBytes(resultBytes));
}

function getDay(timestamp) {
    return Math.floor(timestamp / DAY_MILISECONDS);
}

function getEpoch(timestamp) {
    return Math.floor((timestamp % DAY_MILISECONDS) / EPOCH_MILISECONDS);
}

function getEphemeral(timestamp) {
    return Math.floor((timestamp % EPOCH_MILISECONDS) / EPHEMERAL_MILISECONDS);
}

function randomString(length) {
    var str = "";
    var i;
    for (i = 0; i < length; i += 1) {
        str += String.fromCharCode(Math.floor(Math.random() * 256));
    }
    return str;
}

function generateMasterKey() {
    return randomString(16);
}

function generateIdentityKey(masterKey) {
    return hmac16(masterKey, "IdentityKey");
}

function generateMasterCommitmentKey(identityKey, userId) {
    return hmac16(identityKey, userId + "IdentityCommitment");
}

function generateMasterVerificationKey(masterKey) {
    return hmac16(masterKey, "VerificationKey");
}

function generateFirstDayMasterKey(masterKey) {
    return hmac16(masterKey, "DeriveMasterFirstKey");
}

function rotateDayMasterKey(prevKey) {
    return hmac16(prevKey, "DeriveMasterKey");
}

function getTimeMiliseconds() {
    return Date.now();
}

function generateDayKey(dayMasterKey) {
    return hmac16(dayMasterKey, "DeriveDayKey");
}

function generateDayVerificationKey(masterVerificationKey, day) {
    var dayVerString = intToString(day) + "DeriveVerificationKey";
    return hmac16(masterVerificationKey, dayVerString);
}

function generateDailyCommitmentKey(masterCommitmentKey, day) {
    return encrypt(masterCommitmentKey, pad16(intToString(day)));
}

function generateEpochPreKey(dayKey, day, epoch) {
    var preepochString = intToString(day) + byteToString(epoch);
    return encrypt(dayKey, pad16(preepochString));
}

function generateEpochKey(dailyCommitmentKey, epochPreKey, day, epoch) {
    var epochString = (dailyCommitmentKey + intToString(day) +
                       byteToString(epoch) + "DeriveEpoch");
    return hmac16(epochPreKey, epochString);
}

function generateEpochEncKey(epochKey, day, epoch) {
    var epochEncString = intToString(day) + byteToString(epoch);
    return encrypt(epochKey, pad16(epochEncString));
}

function generateEpochMacKey(epochKey, day, epoch) {
    var fullEpochString = intToString(day) + byteToString(epoch);
    var epochMacString = fullEpochString + String.fromCharCode(1);
    return encrypt(epochKey, pad16(epochMacString));
}

function generateEpochVerKey(dayVerificationKey, day, epoch) {
    var epochVerString = intToString(day) + byteToString(epoch);
    return encrypt(dayVerificationKey, pad16(epochVerString));
}

export function generateEphemeralId(epochEncKey, epochMacKey, epochVerKey,
                                    geoHash, ephemeral) {
    var mask = encrypt(epochEncKey, pad16(numToString(ephemeral, 4)));
    var userRand = epochVerKey.substr(0, 4);
    var plain = pad16(unhex("000000") + geoHash + userRand);
    var xored = xor_strings(plain, mask);
    return xored.substr(0, 12) + encrypt(epochMacKey, xored).substr(0, 4);
}

function isAdjacent(geoHash1, geoHash2) {
    return true;
}

export function checkEpochKey(epochKey, day, epoch, recievedIds) {
    var epochEncKey = generateEpochEncKey(epochKey, day, epoch);
    var epochMacKey = generateEpochMacKey(epochKey, day, epoch);
    var proofs = [];
    var s;
    var mask;
    var i;
    var geoHashForId;
    var recoveredXored;
    var recoveredPlain;
    var mac;
    for (s = 0; s < EPOCH_MILISECONDS / EPHEMERAL_MILISECONDS; s += 1) {
        mask = encrypt(epochEncKey, pad16(numToString(s, 4)));
        for (i = 0; i < recievedIds.length; i += 1) {
            if (mask.substr(0, 3) === recievedIds[i].substr(0, 3)) {
                recoveredPlain = xor_strings(recievedIds[i], mask);
                geoHashForId = recoveredPlain.substr(3, 5);
                if (isAdjacent(geoHashForId, undefined)) {
                    recoveredXored = (recievedIds[i].substr(0, 12) +
                                      mask.substr(12, 4));
                    mac = encrypt(epochMacKey, recoveredXored).substr(0, 4);
                    if (mac === recievedIds[i].substr(12, 4)) {
                        proofs = proofs.concat(recievedIds[i]);
                    }
                }
            }
        }
    }
    return proofs;
}

export var KeyStateManager = function (userId) {
    var ksm = {};
    var day = getDay(getTimeMiliseconds());
    ksm.masterKey = generateMasterKey();
    ksm.identityKey = generateIdentityKey(ksm.masterKey);
    ksm.masterCommitmentKey = generateMasterCommitmentKey(ksm.identityKey,
                                                          userId);
    ksm.masterVerificationKey = generateMasterVerificationKey(ksm.masterKey);
    ksm.dayMasterKeys = [generateFirstDayMasterKey(ksm.masterKey)];
    var i;
    var lastMasterKey;
    var nextMasterKey;
    for (i = 0; i < SAVE_FORWARD_DAYS; i += 1) {
        lastMasterKey = ksm.dayMasterKeys[ksm.dayMasterKeys.length-1];
        nextMasterKey = rotateDayMasterKey(lastMasterKey);
        ksm.dayMasterKeys = ksm.dayMasterKeys.concat(nextMasterKey);
    }
    ksm.prevMasterKey = ksm.dayMasterKeys[0];
    ksm.dayForDayMasterKey = day;
    ksm.dayForPrevMasterKey = day;
    ksm.dayKeys = function (day) {
        var lastMasterKeyDay = ksm.dayForDayMasterKey + SAVE_FORWARD_DAYS;
        var day_iterate;
        var lastDayToSave;
        var lastMasterKey;
        var nextMasterKey;
        for (day_iterate = lastMasterKeyDay; day_iterate < day;
                 day_iterate+= 1) {
            lastMasterKey = ksm.dayMasterKeys[ksm.dayMasterKeys.length-1];
            nextMasterKey = rotateDayMasterKey(lastMasterKey);
            ksm.dayMasterKeys = ksm.dayMasterKeys.slice(1);
            ksm.dayMasterKeys = ksm.dayMasterKeys.concat(nextMasterKey);
            ksm.dayForDayMasterKey += 1;
            lastDayToSave = ksm.dayForDayMasterKey - SAVE_BACK_DAYS_AMOUNT;
            if (lastDayToSave > ksm.dayForPrevMasterKey) {
                ksm.prevMasterKey = rotateDayMasterKey(ksm.prevMasterKey);
                ksm.dayForPrevMasterKey += 1;
            }
        }
        var masterKey = ksm.dayMasterKeys[day - ksm.dayForDayMasterKey];
        var dayKey = generateDayKey(masterKey);
        var dayVerKey = generateDayVerificationKey(ksm.masterVerificationKey,
                                                   day);
        var dayComKey = generateDailyCommitmentKey(ksm.masterCommitmentKey,
                                                   day);
        return [dayKey, dayVerKey, dayComKey];
    };
    ksm.epochKeys = function (day, epoch) {
        var dayKeys = ksm.dayKeys(day);
        var dayKey = dayKeys[0];
        var dayVerKey = dayKeys[1];
        var dayComKey = dayKeys[2];
        var epochPreKey = generateEpochPreKey(dayKey, day, epoch);
        var epochKey = generateEpochKey(dayComKey, epochPreKey, day, epoch);
        var epochEncKey = generateEpochEncKey(epochKey, day, epoch);
        var epochMacKey = generateEpochMacKey(epochKey, day, epoch);
        var epochVerKey = generateEpochVerKey(dayVerKey, day, epoch);
        return [epochEncKey, epochMacKey, epochVerKey];
    };
    ksm.ephemeralId = function (geoHash) {
        var timestamp = getTimeMiliseconds();
        var currentDay = getDay(timestamp);
        var epoch = getEpoch(timestamp);
        var ephemeral = getEphemeral(timestamp);
        var epochKeys = ksm.epochKeys(currentDay, epoch);
        var epochEncKey = epochKeys[0];
        var epochMacKey = epochKeys[1];
        var epochVerKey = epochKeys[2];
        return generateEphemeralId(epochEncKey, epochMacKey, epochVerKey,
                                   geoHash, ephemeral);
    };
    ksm.outputToMoh = function () {
        return [ksm.dayForPrevMasterKey, ksm.prevMasterKey,
                        ksm.masterVerificationKey];
    };
    ksm.exportState = function () {
        var keys = [ksm.masterKey, ksm.identityKey, ksm.masterCommitmentKey,
                    ksm.masterVerificationKey, ksm.dayMasterKeys[0],
                    ksm.prevMasterKey, ksm.dayForDayMasterKey,
                    ksm.dayForPrevMasterKey];
        return keys.map(x => btoa(x));
    }
    ksm.importState = function (keysArr) {
        var decodedKeys = keysArr.map(x => atob(x));
        ksm.masterKey = decodedKeys[0];
        ksm.identityKey = decodedKeys[1];
        ksm.masterCommitmentKey = decodedKeys[2];
        ksm.masterVerificationKey = decodedKeys[3];
        ksm.dayMasterKeys = [decodedKeys[4]];
        for (i = 0; i < SAVE_FORWARD_DAYS; i += 1) {
            lastMasterKey = ksm.dayMasterKeys[ksm.dayMasterKeys.length-1];
            nextMasterKey = rotateDayMasterKey(lastMasterKey);
            ksm.dayMasterKeys = ksm.dayMasterKeys.concat(nextMasterKey);
        }
        ksm.prevMasterKey = decodedKeys[5];
        ksm.dayForDayMasterKey = parseInt(decodedKeys[6]);
        ksm.dayForPrevMasterKey = parseInt(decodedKeys[7]);
    }
    ksm.generateEpochKeys = function () {
        var timestamp = getTimeMiliseconds();
        var startDay = getDay(timestamp);
        var currentDay;
        var i;
        var j;
        var keyArray = [];
        var epochKeys;
        var epochEncKey;
        var epochMacKey;
        var epochVerKey;
        for (i = 0; i <= SAVE_FORWARD_DAYS; i += 1) {
            for (j = 0; j < DAY_MILISECONDS / EPOCH_MILISECONDS; j += 1) {
                currentDay = startDay + i;
                epochKeys = ksm.epochKeys(currentDay, j)
                var epochEncKey = epochKeys[0];
                var epochMacKey = epochKeys[1];
                var epochVerKey = epochKeys[2];
                keyArray = keyArray.concat({day: currentDay, epoch: j,
                                            epochEncKey: btoa(epochEncKey),
                                            epochMacKey: btoa(epochMacKey),
                                            epochVerKey: btoa(epochVerKey)});
            }
        }
        return keyArray;
    }
    return ksm;
};
