import React, {useEffect, useState} from 'react';
import {
    NativeEventEmitter,
    FlatList,
    StyleSheet
} from 'react-native';
import SpecialBle from 'rn-contact-tracing';
import {View, Card, Button, Text, ListItem, Colors} from 'react-native-ui-lib';

const deleteIcon = require('../res/delete.png');
const shareIcon = require('../res/share.png');

function ResultsScreen() {

    const [scans, setScans] = useState([]);

    useEffect(() => {
        const eventEmitter = new NativeEventEmitter(SpecialBle);
        eventEmitter.addListener('foundScan', (event) => {
                console.log(event);
                _getAllScansFromDB();
            },
        );
        _getAllScansFromDB();
    }, []);


    // get all scans from DB
    async function _getAllScansFromDB() {
        SpecialBle.getAllScans((scans) => {
            setScans(scans)
        })
    }

    // clean all scans from DB
    function _cleanAllScansFromDB() {
        SpecialBle.cleanScansDB();
        _getAllScansFromDB();
    }


    return (
        <View style={styles.container}>

            <View spread style={styles.topContainer}>
                <Text style={{fontSize: 30, fontWeight: 'bold'}}>Detected Scans</Text>
                <View style={styles.topContainerButtons}>
                    <Button text90 link green10 iconSource={shareIcon} style={{paddingHorizontal: 10}}/>
                    <Button text90 link red10 iconSource={deleteIcon} onPress={_cleanAllScansFromDB} style={{paddingHorizontal: 10}}/>
                </View>
            </View>
            <FlatList
                data={scans}
                keyExtractor={item => item.public_key}
                renderItem={({item, index}) => _renderListItem(item, index)}
            />

        </View>
    );

    function timeStampToUTCTime(timestamp){
        let date = new Date(timestamp)
        let res =  date.toLocaleDateString()+'-'+date.getUTCHours()+':'+date.getUTCMinutes()+':'+date.getUTCSeconds()+'.'+date.getUTCMilliseconds();
        return res;
    }

    function _renderListItem(item, id) {
        let ts = timeStampToUTCTime(item.scan_timestamp);
        return (
            <Card row height={50} style={styles.card}>
                <View flex row center-vertical>
                    <ListItem.Part middle column>
                        <ListItem.Part containerStyle={{marginBottom: 2}}>
                            <Text dark10 text70>EphId: { ((item.public_key).length > 10) ? (((item.public_key).substring(0,10-3)) + '...') : item.public_key }</Text>
                            <Text dark10 text70>{item.scan_protocol}</Text>
                            <Text dark10 text70>{ts}</Text>
                        </ListItem.Part>
                        <ListItem.Part containerStyle={{marginBottom: 2}}>
                            <Text text90 color='green'>RSSI: {item.scan_rssi}</Text>
                            <Text text90 color='red'>TX: {item.scan_tx}</Text>
                            <Text text90 color='red'>ESTD: ? m </Text>
                        </ListItem.Part>
                    </ListItem.Part>
                </View>
            </Card>
        );

    }
};


const styles = StyleSheet.create({
    container: {
        flex: 1,
        marginHorizontal: 5,
    },
    topContainer: {
        marginTop: 20,
        marginBottom: 10,
        paddingHorizontal:10,
        flexDirection: 'row',
    },
    topContainerButtons: {
        alignItems: 'flex-start',
        flexDirection: 'row',
    },

    timeText: {
        padding: 5,
    },
    dataText: {
        padding: 5,
    },
    card: {
        padding: 5,
        marginVertical: 5
    }
});

export default ResultsScreen;
