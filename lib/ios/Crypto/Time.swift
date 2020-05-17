//
//  Time.swift
//
//  Created by hagai rothschild on 10/05/2020.
//  Copyright © 2020 iGates. All rights reserved.
//

import Foundation



class Time: Equatable, Hashable, Codable
{
    
    var time: Int;
    var epoch: Int;
    var day: Int;
        
    init(_ unix_time: Int)
        {
            self.time = unix_time
            self.day = unix_time / Constants.T_DAY
            self.epoch = (unix_time % Constants.T_DAY) / Constants.T_EPOCH
        }
    
    init(_ unix_time: Int, _ epoch: Int)
    {
            //let str = String(format:"Epoch %d must be <  %d", epoch!, const_EPOCHS_IN_DAY)
            // TODO: Assert
//            XCTAssert(epoch! < const_EPOCHS_IN_DAY, str);
            self.day = unix_time
            self.epoch = Int(epoch)
            self.time = 0 // unix_time;///??????
    }
    
    class func day_to_second(day: Int) -> Int
    {
        return Constants.T_DAY * day;
    }
 
    func get_units() -> Int
    {
        return (self.time - self.day * Constants.T_DAY - self.epoch * Constants.T_EPOCH) / Constants.T_UNIT
    }
    
    func get_next() -> Time
    {
        if (self.epoch == Constants.EPOCHS_IN_DAY-1)
        {
            return Time(self.day+1, 0);
        }
        return Time(self.day, self.epoch+1);
    }
    
    func str_with_units() -> String
    {
    // for hashing-including-unit purposes
        return String(format:"{%d}-{%d}-{%d}",self.day, self.epoch, self.get_units());
    }
    
    static func == (lhs: Time, rhs: Time) -> Bool
    {
        return lhs.day == rhs.day && lhs.epoch == rhs.epoch;
    }
    
    static func != (lhs: Time, rhs: Time) -> Bool
    {
        return lhs.day != rhs.day || lhs.epoch != rhs.epoch;
    }
    
    static func < (lhs: Time, rhs: Time) -> Bool
    {
        return Constants.EPOCHS_IN_DAY*lhs.day + lhs.epoch < Constants.EPOCHS_IN_DAY*rhs.day + rhs.epoch;
    }
    
    static func <= (lhs: Time, rhs: Time) -> Bool
    {
        return Constants.EPOCHS_IN_DAY*lhs.day + lhs.epoch <= Constants.EPOCHS_IN_DAY*rhs.day + rhs.epoch;
    }
    
    static func > (lhs: Time, rhs: Time) -> Bool
    {
        return Constants.EPOCHS_IN_DAY*lhs.day + lhs.epoch > Constants.EPOCHS_IN_DAY*rhs.day + rhs.epoch;
    }
    
    static func >= (lhs: Time, rhs: Time) -> Bool
    {
        return Constants.EPOCHS_IN_DAY*lhs.day + lhs.epoch >= Constants.EPOCHS_IN_DAY*rhs.day + rhs.epoch;
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(day)
        hasher.combine(epoch)
    }
//TODO:: missing hash impl
//    def __hash__(self) -> hash:
//        return hash((self.day, self.epoch))

}
