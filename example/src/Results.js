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

    const [devices, setDevices] = useState([]);

    useEffect(() => {
        const eventEmitter = new NativeEventEmitter(SpecialBle);
        eventEmitter.addListener('foundDevice', (event) => {
                console.log(event);
                _getAllDevicesFromDB();
            },
        );
        _getAllDevicesFromDB();
    }, []);


    // get all devices from DB
    async function _getAllDevicesFromDB() {
        SpecialBle.getAllDevices((devices) => {
            setDevices(devices)
        })
    }

    // clean all devices from DB
    function _cleanAllDevicesFromDB() {
        SpecialBle.cleanDevicesDB();
        _getAllDevicesFromDB();
    }


    return (
        <View style={styles.container}>

            <View spread style={styles.topContainer}>
                <Text style={{fontSize: 30, fontWeight: 'bold'}}>Detected Contacts</Text>
                <View style={styles.topContainerButtons}>
                    <Button text90 link green10 iconSource={shareIcon} onPress={_cleanAllDevicesFromDB} style={{paddingHorizontal: 10}}/>
                    <Button text90 link red10 iconSource={deleteIcon}  style={{paddingHorizontal: 10}}/>
                </View>
            </View>
            <FlatList
                data={devices}
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
        let ts = timeStampToUTCTime(item.device_timestamp);
        return (
            <Card row height={50} style={styles.card}>
                <View flex row center-vertical>
                    <ListItem.Part middle column>
                        <ListItem.Part containerStyle={{marginBottom: 2}}>
                            <Text dark10 text70>EphId: { ((item.public_key).length > 10) ? (((item.public_key).substring(0,10-3)) + '...') : item.public_key }</Text>
                            <Text dark10 text70>{item.device_protocol}</Text>
                            <Text dark10 text70>{ts}</Text>
                        </ListItem.Part>
                        <ListItem.Part containerStyle={{marginBottom: 2}}>
                            <Text text90 color='green'>RSSI: {item.device_rssi}</Text>
                            <Text text90 color='red'>TX: {item.device_tx}</Text>
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
