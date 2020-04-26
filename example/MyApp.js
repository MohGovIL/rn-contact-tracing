import React, { PureComponent } from "react";
import { ScrollView, TextInput, Button, Text, View } from 'react-native';
import { KeyStateManager } from "./HamagenCrypto.js";

export default class App extends PureComponent {
    state = {
        userId: '',
        ksm: new KeyStateManager(''),
        textContents: []
    };
    onChangeUserId = (newUserId) => {
        this.setState({
            userId: newUserId
        })
    }
    regenerateKsmPressed = () => {
        this.setState({
            ksm: new KeyStateManager(this.state.userId)
        })
    }
    viewPressed = () => {
        var keys = this.state.ksm.exportState();
        this.setState({
            textContents: keys
        })
    }
    generateEpochKeysPressed = () => {
        var i;
        var keys = this.state.ksm.generateEpochKeys();
        this.setState({
            textContents: keys
        })
    }
    render() {
        return (
            <View style={{paddingVertical: 25, paddingHorizontal: 10}}>
                <Text>Enter user ID below (defaults to empty string):</Text>
                <TextInput style={{borderColor: 'gray', borderWidth: 1}} onChangeText={text => this.onChangeUserId(text)} />
                <Button color="hotpink" title="Regenerate key state" onPress={this.regenerateKsmPressed} />
                <Button color="purple" title="View key state" onPress={this.viewPressed} />
                <Button color="blue" title="Generate epohc keys" onPress={this.generateEpochKeysPressed} />
                <ScrollView>
                {this.state.textContents.map((value, key) => (
                    <Text style={{paddingTop: 10}} key={key}>{JSON.stringify(value)}</Text>
                ))}
                </ScrollView>
            </View>
        );
    }
}
