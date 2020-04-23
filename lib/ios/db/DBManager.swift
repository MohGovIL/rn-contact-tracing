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

            if let bundle = Bundle(identifier: "com.rn-contact-tracing.Framework") {
                return bundle
            }

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
            data.setValue(attributes[key], forKeyPath: key)
        }
        do {
            try managedContext.save()
        } catch let error as NSError {
            print("Could not save. \(error), \(error.userInfo)")
        }
    }
    
    func getEntityWithPredicate(entity:String, predicateKey:String, predicateValue:String) -> NSManagedObject? {
        
        var data: [NSManagedObject] = []
        let managedContext = self.persistentContainer.viewContext
        let fetchRequest = NSFetchRequest<NSManagedObject>(entityName: entity)
        fetchRequest.predicate = NSPredicate(format: "%@ == %@", predicateKey, predicateValue)
        
        do {
          data = try managedContext.fetch(fetchRequest)
        } catch let error as NSError {
          print("Could not fetch. \(error), \(error.userInfo)")
        }
        
        return data.first
    }
    
    func getAll(_ entity:String) -> Array<Any> {

        var coreData: [CoreDataCodable] = []
        var array = Array<Any>()
        let managedContext = self.persistentContainer.viewContext
        let fetchRequest = NSFetchRequest<NSManagedObject>(entityName: entity)
        
        guard let codingUserInfoKeyManagedObjectContext = CodingUserInfoKey.managedObjectContext else {
            fatalError("Failed to retrieve context")
        }
        
        let jsonEncoder = JSONEncoder()
        jsonEncoder.outputFormatting = .prettyPrinted
        
        do {
            coreData = try managedContext.fetch(fetchRequest) as! [CoreDataCodable]
            jsonEncoder.userInfo[codingUserInfoKeyManagedObjectContext] = managedContext
            let data = try jsonEncoder.encode([coreData])
            array = try JSONSerialization.jsonObject(with: data, options: []) as! Array<Any>
        } catch let error as NSError {
          print("Could not fetch. \(error), \(error.userInfo)")
        }

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
}
