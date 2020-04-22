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
    
    fileprivate lazy var managedObjectModel: NSManagedObjectModel = {

    // the moc's model should be accessible via apps in this workspace
    // or through the module that cocoapods will create as part of the file's
    // resource bundle, as such, we need to look in two different places
    // to use the correct bundle at run time
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
        return NSManagedObjectModel()
    }

    guard
        let modelURL = bundle.url(forResource: "Model", withExtension: "momd"),
        let model = NSManagedObjectModel(contentsOf: modelURL) else {
            print("Could not get bundle for managed object model")
            return NSManagedObjectModel()
    }

    return model
    }()

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
    
    func getAll(_ entity:String) -> [NSManagedObject] {

        var data: [NSManagedObject] = []
        let managedContext = self.persistentContainer.viewContext
        let fetchRequest = NSFetchRequest<NSManagedObject>(entityName: entity)
        
        do {
          data = try managedContext.fetch(fetchRequest)
        } catch let error as NSError {
          print("Could not fetch. \(error), \(error.userInfo)")
        }
        return data
    }
    
    func deleteAllData(_ entity:String) {
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: entity)
        fetchRequest.returnsObjectsAsFaults = false
        let managedContext = self.persistentContainer.viewContext
        do {
            let results = try managedContext.fetch(fetchRequest)
            for object in results {
                guard let objectData = object as? NSManagedObject else {continue}
                managedContext.delete(objectData)
            }
        } catch let error {
            print("Detele all data in \(entity) error :", error)
        }
    }
    
}
