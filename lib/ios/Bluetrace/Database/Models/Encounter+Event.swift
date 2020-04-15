//
//  Encounter+Event.swift
//  OpenTrace

import UIKit
import CoreData

extension Encounter {

    enum Event: String {
        case scanningStarted = "Scanning started"
        case scanningStopped = "Scanning stopped"
    }

    static func saveWithCurrentTime(for event: Event) {
        DispatchQueue.main.async {
            let managedContext = ConfigureDatabaseManager.shared().persistentContainer.viewContext
            let entity = NSEntityDescription.entity(forEntityName: "Encounter", in: managedContext)!
            let encounter = Encounter(entity: entity, insertInto: managedContext)
            encounter.msg = event.rawValue
            encounter.timestamp = Date()
            encounter.v = nil
            do {
                try managedContext.save()
            } catch {
                print("Could not save. \(error)")
            }
        }
    }

}
