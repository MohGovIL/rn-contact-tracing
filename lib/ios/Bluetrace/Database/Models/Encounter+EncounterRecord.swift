//
//  Encounter+EncounterRecord.swift
//  OpenTrace

import UIKit
import CoreData

extension EncounterRecord {

    func saveToCoreData() {
        DispatchQueue.main.async {
            let managedContext = ConfigureDatabaseManager.shared().persistentContainer.viewContext
            let entity = NSEntityDescription.entity(forEntityName: "Encounter", in: managedContext)!
            let encounter = Encounter(entity: entity, insertInto: managedContext)
            encounter.set(encounterStruct: self)
            do {
                try managedContext.save()
            } catch {
                print("Could not save. \(error)")
            }
        }
    }

}
