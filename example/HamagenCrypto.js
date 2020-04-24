var DAY_MILISECONDS = 24 * 60 * 60 * 1000;
var EPOCH_MILISECONDS = 60 * 60 * 1000;
var EPHEMERAL_MILISECONDS = 5 * 60 * 1000;
var MASTER_KEY_LENGTH = 16;
var SAVE_BACK_DAYS_AMOUNT = 14;

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

function hmac(key, data) {
    return unhex(new CryptoJS.HmacSHA256(key, data).toString());
}

function encrypt(key, data) {
    var crypt_params = {mode: CryptoJS.mode.ECB,
                        padding: CryptoJS.pad.NoPadding};
    var result = CryptoJS.AES.encrypt(data, CryptoJS.enc.Latin1.parse(key),
                                      crypt_params);
    var ciphertext_hex = result.ciphertext.toString();
    return unhex(ciphertext_hex);
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
    var data = new Uint8Array(length);
    crypto.getRandomValues(data);
    var i;
    for (i = 0; i < length; i += 1) {
        str += String.fromCharCode(data[i]);
    }
    return str;
}

function generateMasterKey() {
    return randomString(MASTER_KEY_LENGTH);
}

function generateIdentityKey(masterKey) {
    return hmac(masterKey, "IdentityKey").substr(0, 16);
}

function generateMasterCommitmentKey(identityKey, userId) {
    return hmac(identityKey, userId + "IdentityCommitment");
}

function generateMasterVerificationKey(masterMey) {
    return hmac(masterMey, "VerificationKey");
}

function generateFirstDayMasterKey(masterKey) {
    return hmac(masterKey, "DeriveMasterFirstKey");
}

function rotateDayMasterKey(prevKey) {
    return hmac(prevKey, "DeriveMasterKey");
}

function getTimeMiliseconds() {
    return Date.now();
}

function generateDayKey(dayMasterKey) {
    return hmac(dayMasterKey, "DeriveDayKey");
}

function generateDayVerificationKey(masterVerificationKey, day) {
    var dayVerString = intToString(day) + "DeriveVerificationKey";
    return hmac(masterVerificationKey, dayVerString);
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
    return hmac(epochPreKey, epochString);
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

function generateEphemeralId(epochEncKey, epochMacKey, epochVerKey, geoHash,
                                                         ephemeral) {
    var mask = encrypt(epochEncKey, numToString(ephemeral, 16));
    var userRand = epochVerKey.substr(0, 4);
    var plain = pad16(unhex("000000") + geoHash + userRand);
    var xored = xor_strings(plain, mask);
    return xored.substr(0, 12) + encrypt(epochMacKey, xored).substr(0, 4);
}

function isAdjacent(geoHash1, geoHash2) {
    return true;
}

function checkEpochKey(epochKey, day, epoch, recievedIds) {
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
        mask = encrypt(epochEncKey, numToString(s, 16));
        for (i = 0; i < recievedIds.length; i += 1) {
            if (mask.substr(0, 3) === recievedIds[i].substr(0, 3)) {
                recoveredPlain = xor_strings(recievedIds[i], mask);
                geoHashForId = recoveredPlain.substr(3, 8);
                if (isAdjacent(geoHashForId, undefined)) {
                    recoveredXored = (recievedIds[i].substr(0, 12) +
                                      mask.substr(12, 16));
                    mac = encrypt(epochMacKey, recoveredXored).substr(0, 4);
                    if (mac === recievedIds[i].substr(12, 16)) {
                        proofs.concat(recievedIds[i]);
                    }
                }
            }
        }
    }
    return proofs;
}

var KeyStateManager = function (userId) {
    var ksm = {};
    var day = getDay(getTimeMiliseconds());
    ksm.masterKey = generateMasterKey();
    ksm.identityKey = generateIdentityKey(ksm.masterKey);
    ksm.masterCommitmentKey = generateMasterCommitmentKey(ksm.identityKey,
                                                          userId);
    ksm.masterVerificationKey = generateMasterVerificationKey(ksm.masterMey);
    ksm.dayMasterKey = generateFirstDayMasterKey(ksm.masterKey);
    ksm.prevMasterKey = ksm.dayMasterKey;
    ksm.dayForDayMasterKey = day;
    ksm.dayForPrevMasterKey = day;
    ksm.dayMasterKeys = function (day) {
        var oldDayForMasterKey = ksm.dayForDayMasterKey;
        var day_iterate;
        var lastDayToSave;
        for (day_iterate = oldDayForMasterKey; day_iterate < day;
                 day_iterate+= 1) {
            ksm.dayMasterKey = rotateDayMasterKey(ksm.dayMasterKey);
            ksm.dayForDayMasterKey += 1;
            lastDayToSave = ksm.dayForDayMasterKey - SAVE_BACK_DAYS_AMOUNT;
            if (lastDayToSave > ksm.dayForPrevMasterKey) {
                ksm.prevMasterKey = rotateDayMasterKey(ksm.prevMasterKey);
                ksm.dayForPrevMasterKey += 1;
            }
        }
        var dayKey = generateDayKey(ksm.dayMasterKey);
        var dayVerKey = generateDayVerificationKey(ksm.masterVerificationKey,
                                                   day);
        var dayComKey = generateDailyCommitmentKey(ksm.masterCommitmentKey,
                                                   day);
        return [dayKey, dayVerKey, dayComKey];
    };
    ksm.epochKeys = function (day, epoch) {
        var dayKeys = ksm.dayMasterKeys(day);
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
    return ksm;
};
