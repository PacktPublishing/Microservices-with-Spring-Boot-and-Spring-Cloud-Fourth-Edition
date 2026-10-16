$ErrorActionPreference = 'Stop'

$common = @(
  '--boot-version=4.1.1',
  '--type=gradle-project-kotlin',
  '--java-version=25',
  '--packaging=jar',
  '--group-id=com.minhhung.producthub',
  '--version=0.0.1-SNAPSHOT'
)
$coreDeps = 'web,actuator,validation,springdoc-openapi'

spring init @common --name=product-service --artifact-id=product-service `
  --package-name=com.minhhung.producthub.product --dependencies=$coreDeps product-service

spring init @common --name=recommendation-service --artifact-id=recommendation-service `
  --package-name=com.minhhung.producthub.recommendation --dependencies=$coreDeps recommendation-service

spring init @common --name=review-service --artifact-id=review-service `
  --package-name=com.minhhung.producthub.review --dependencies=$coreDeps review-service

spring init @common --name=product-composite-service --artifact-id=product-composite-service `
  --package-name=com.minhhung.producthub.composite --dependencies="$coreDeps,cloud-feign" product-composite-service
