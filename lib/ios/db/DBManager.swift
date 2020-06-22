//
//  DBManager.swift
//  rn-contact-tracing
//
//  Created by Tzufit Lifshitz on 4/19/20.
//

import Foundation
import CoreData

class DBManager {
    
    static let shared = DBManager()

    
    // MARK: - Core Data stack

    lazy var persistentContainer: NSPersistentContainer = {
        
        var rawBundle: Bundle? {

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
    
    func save(entity:String, attributes: [String:Any]) {
      
        let managedContext = self.persistentContainer.viewContext

        let entity = NSEntityDescription.entity(forEntityName: entity, in: managedContext)!
        let data = NSManagedObject(entity: entity, insertInto: managedContext)
        
        for key in attributes.keys {
            if let value = attributes[key] {
                data.setValue(value, forKeyPath: key)
            }
        }
        do {
            try managedContext.save()
        } catch let error as NSError {
            print("Could not save. \(error), \(error.userInfo)")
        }
    }
    
    func updateDevice(attributes: [String:Any]){
        let managedContext = self.persistentContainer.viewContext
        let fetchRequest = NSFetchRequest<NSManagedObject>(entityName: "Device")
        fetchRequest.predicate = NSPredicate(format: "public_key == %@", attributes["public_key"] as! CVarArg)

        do {
            let results = try managedContext.fetch(fetchRequest) as! [Device]
            if results.count != 0 {
                results[0].setValue(attributes["device_last_timestamp"], forKey: "device_last_timestamp")
                results[0].setValue(attributes["device_rssi"], forKey: "device_rssi")
            }
        } catch {
            print("Fetch Failed: \(error)")
        }

        do {
            try managedContext.save()
           }
        catch {
            print("Saving Core Data Failed: \(error)")
        }
    }
    
    func getEntityWithPredicate(entity:String, predicateKey:String, predicateValue:String) -> NSArray {
        
        let fetchRequest = NSFetchRequest<NSManagedObject>(entityName: entity)
        let predicateFormat = String(format: "%@ == %@", predicateKey, "%@")
        fetchRequest.predicate = NSPredicate(format: predicateFormat, predicateValue)

        let array = convertCoreDataArrayToData(fetchRequest: fetchRequest)
        
        return array
    }
    
    func getFetchedResults(_ entity: String) -> NSFetchedResultsController<NSManagedObject> {
        let fetchRequest = NSFetchRequest<NSManagedObject>(entityName: entity)
        let sortDescriptor = NSSortDescriptor(key: "timestamp", ascending: true)
        fetchRequest.sortDescriptors = [sortDescriptor]
        
        return NSFetchedResultsController(fetchRequest: fetchRequest, managedObjectContext: self.persistentContainer.viewContext, sectionNameKeyPath: nil, cacheName: nil)
    }
    
    func getAll(_ entity:String) -> NSArray {

        let fetchRequest = NSFetchRequest<NSManagedObject>(entityName: entity)
        
        let array = convertCoreDataArrayToData(fetchRequest: fetchRequest)

        return array
    }
    
    func deleteAllData(_ entity:String) {
        let managedContext = self.persistentContainer.viewContext
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: entity)
        let batchDeleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
        do {
            try managedContext.execute(batchDeleteRequest)
        } catch let error as NSError {
            print("Detele all data in \(entity) error :", error)
        }
    }
    
    func convertCoreDataArrayToData(fetchRequest:NSFetchRequest<NSManagedObject>) -> NSArray {
        var array = NSArray()
        var coreData: [CoreDataCodable] = []
        let managedContext = self.persistentContainer.viewContext
        
        guard let codingUserInfoKeyManagedObjectContext = CodingUserInfoKey.managedObjectContext else {
            fatalError("Failed to retrieve context")
        }
        
        let jsonEncoder = JSONEncoder()
        jsonEncoder.outputFormatting = .prettyPrinted
        
        do {
            coreData = try managedContext.fetch(fetchRequest) as! [CoreDataCodable]
            jsonEncoder.userInfo[codingUserInfoKeyManagedObjectContext] = managedContext
            let data = try jsonEncoder.encode(coreData)
            array = try JSONSerialization.jsonObject(with: data, options: []) as! NSArray
        } catch let error as NSError {
          print("Could not fetch. \(error), \(error.userInfo)")
        }
        return array
    }

    func updateScan(attributes: [String:Any]){
        // TODO: TBD
    }
}
