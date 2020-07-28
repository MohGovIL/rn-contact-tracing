require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "rn-contact-tracing"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  rn-contact-tracing
                   DESC
  s.homepage     = "https://github.com/MohGovIL/rn-contact-tracing"
  # brief license entry:
  s.license      = "MIT"
  # optional - use expanded license entry instead:
  # s.license    = { :type => "MIT", :file => "LICENSE" }
  s.authors      = { "Ofer Davidyan" => "ofer@igates.co.il",
                     "Yonatan Rimon" => "yoni.rimon@igates.co.il",
                     "Hagai Rotshild" => "hagai.rotshild@igates.co.il",
                     "Lev" => "lev.vidrak@gmail.com"
  }
  s.platforms    = { :ios => "10.0" }
  s.source       = { :git => "https://github.com/MohGovIL/rn-contact-tracing.git", :tag => "#{s.version}" }

  s.source_files = "lib/ios/**/*.{h,m,swift}"
  s.resources = 'rn-contact-tracing/*.xcdatamodel'
  s.resource_bundles = { 'FrameworkModel' => ['lib/ios/db/CoreData/**/*.xcdatamodeld'] }
  s.requires_arc = true

  s.dependency "React"
  # ...
  # s.dependency "..."
end
