//
//  DBContactManager.swift
//  rn-contact-tracing
//
//  Created by Yonatan Rimon on 14/05/2020.
//

import Foundation
import CoreData

class DBContactManager {
    
    static let shared = DBContactManager()
    
    // MARK: - Core Data stack

    lazy var persistentContainer: NSPersistentContainer = {
        
        var rawBundle: Bundle? {

//            if let bundle = Bundle(identifier: "com.rn-contact-tracing.Framework") {
//                return bundle
//            }

            guard
                let resourceBundleURL = Bundle(for: type(of: self)).url(forResource: "FrameworkModel", withExtension: "bundle"),
                let realBundle = Bundle(url: resourceBundleURL) else {
                    return nil
            }

            return realBundle
        }

        guard let bundle = rawBundle else {
            print("Could not get bundle that contains the model ")
            return NSPersistentContainer()
        }
        
        let modelURL = bundle.url(forResource: "Model", withExtension: "momd")

        var container: NSPersistentContainer
        
        guard let model = modelURL.flatMap(NSManagedObjectModel.init) else {
          print("Fail to load the trigger model!")
          return NSPersistentContainer()
        }
        
        container = NSPersistentContainer(name: "Model", managedObjectModel: model)
        container.loadPersistentStores(completionHandler: { (storeDescription, error) in
          if let error = error as NSError? {
            print("Unresolved error \(error), \(error.userInfo)")
          }
        })
        
        return container
    }()
    
    func getContacts() -> NSFetchedResultsController<Contact> {
        let fetchRequest = NSFetchRequest<Contact>(entityName: "Contact")
        let sortDescriptor = NSSortDescriptor(key: "timestamp", ascending: true)
        fetchRequest.sortDescriptors = [sortDescriptor]
        
        let frc = NSFetchedResultsController(fetchRequest: fetchRequest, managedObjectContext: self.persistentContainer.viewContext, sectionNameKeyPath: nil, cacheName: nil)
        do {
            try frc.performFetch()
        }  catch let error as NSError {
           print("Could not fetch. \(error), \(error.userInfo)")
       }
        return frc
    }
    
    func addNewContact(ephemeral_id: [UInt8], rssi: Int, time: Int, location: [UInt8], id: Int, lat: Double, lon: Double) {
        let managedContext = self.persistentContainer.newBackgroundContext()
        let entity = NSEntityDescription.entity(forEntityName: "Contact", in: managedContext)!

        let data = NSManagedObject(entity: entity, insertInto: managedContext)

        data.setValue(Data(ephemeral_id), forKey: "ephemeral_id")
        data.setValue(rssi, forKey: "rssi")
        data.setValue(time, forKey: "timestamp")
        data.setValue(Data(location), forKey: "geohash")
        data.setValue(id, forKey: "id")
        data.setValue(lat, forKey: "lat")
        data.setValue(lon, forKey: "lon")
        do {
            try managedContext.save()
        } catch let error as NSError {
            print("Could not save. \(error), \(error.userInfo)")
        }
    }
    
    func deleteContact(_ contact: Contact) {
        let managedContext = self.persistentContainer.newBackgroundContext()
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Contact")
        let predicate = NSPredicate(format: "SELF == %@", contact)
        fetchRequest.predicate = predicate

        let batchDeleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
        do {
            try managedContext.execute(batchDeleteRequest)
        } catch let error as NSError {
            print("Detele contact error :", error)
        }
    }
    
    func deleteContactsHistory(dtime: Int) {
        let managedContext = self.persistentContainer.newBackgroundContext()
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Contact")
        let predicate = NSPredicate(format: "timestamp < \(dtime)")
        fetchRequest.predicate = predicate

        let batchDeleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
        do {
            try managedContext.execute(batchDeleteRequest)
        } catch let error as NSError {
            print("Detele contact error :", error)
        }
    }
    
    func deleteAllContacts() {
        let managedContext = self.persistentContainer.newBackgroundContext()
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Contact")
        let batchDeleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
        do {
            try managedContext.execute(batchDeleteRequest)
        } catch let error as NSError {
            print("Detele all contacts error :", error)
        }
    }
    
}
