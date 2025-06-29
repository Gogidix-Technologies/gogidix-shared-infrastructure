# Geo Location Service API Documentation

## Core Location Management API

### GeoLocationService
- `GeoLocationService(config)`: Initialize geo location service with configuration
- `GeoLocationService(providers, indexer, intelligence)`: Initialize with specific components
- `geocode(address, options)`: Convert address to geographic coordinates
- `geocode(address, country, language)`: Geocode with country and language constraints
- `reverseGeocode(latitude, longitude, options)`: Convert coordinates to address
- `batchGeocode(addresses, options)`: Geocode multiple addresses efficiently
- `validateAddress(address, validationRules)`: Validate address format and components
- `normalizeAddress(address, normalizationOptions)`: Normalize address format
- `getCurrentLocation(userId)`: Get user's current location
- `updateCurrentLocation(userId, location, timestamp)`: Update user's current location
- `getLocationHistory(userId, dateRange, options)`: Get user's location history
- `storeLocationUpdate(locationUpdate)`: Store location update event
- `calculateDistance(location1, location2, unit)`: Calculate distance between locations
- `calculateBearing(location1, location2)`: Calculate bearing between locations
- `findNearbyLocations(center, radius, filters)`: Find locations within radius
- `isLocationValid(location, validationRules)`: Validate location coordinates

### LocationService
- `LocationService(storage, indexer)`: Initialize location service
- `createLocation(locationData)`: Create new location entity
- `updateLocation(locationId, updates)`: Update location information
- `deleteLocation(locationId)`: Delete location entity
- `getLocation(locationId)`: Get location by ID
- `searchLocations(query, filters)`: Search locations by criteria
- `addLocationTag(locationId, tag)`: Add tag to location
- `removeLocationTag(locationId, tag)`: Remove tag from location
- `setLocationCategory(locationId, category)`: Set location category
- `getLocationsByCategory(category, pagination)`: Get locations by category
- `bulkImportLocations(locationData, importOptions)`: Import locations in bulk
- `exportLocations(query, format)`: Export location data
- `mergeLocations(sourceLocationId, targetLocationId)`: Merge duplicate locations
- `verifyLocation(locationId, verificationData)`: Verify location accuracy
- `getLocationStatistics(filters)`: Get location statistics and metrics

### GeocodingService
- `GeocodingService(providers, cache)`: Initialize geocoding service
- `geocodeAddress(address, geocodingOptions)`: Geocode single address
- `geocodeStructuredAddress(addressComponents, options)`: Geocode structured address
- `reverseGeocode(coordinates, reverseOptions)`: Reverse geocode coordinates
- `batchGeocode(addresses, batchOptions)`: Batch geocode addresses
- `getGeocodingProviders()`: Get available geocoding providers
- `setProviderPriority(providerId, priority)`: Set provider priority
- `enableProvider(providerId)`: Enable geocoding provider
- `disableProvider(providerId)`: Disable geocoding provider
- `getProviderQuota(providerId)`: Get provider usage quota
- `getProviderStatistics(providerId)`: Get provider performance statistics
- `validateGeocodingResult(result, validationRules)`: Validate geocoding result
- `cacheGeocodingResult(address, result, cacheOptions)`: Cache geocoding result
- `getCachedResult(address, cacheOptions)`: Get cached geocoding result
- `clearGeocodingCache(filters)`: Clear geocoding cache

## Spatial Analysis API

### SpatialIndexer
- `SpatialIndexer(indexConfig)`: Initialize spatial indexer
- `indexLocation(location, metadata)`: Add location to spatial index
- `updateLocationIndex(locationId, newLocation)`: Update indexed location
- `removeFromIndex(locationId)`: Remove location from index
- `rebuildIndex(locations)`: Rebuild entire spatial index
- `optimizeIndex()`: Optimize index performance
- `getIndexStatistics()`: Get index statistics and metrics
- `searchByRadius(center, radius, filters)`: Search locations within radius
- `searchByBoundingBox(bounds, filters)`: Search locations within bounding box
- `searchByPolygon(polygon, filters)`: Search locations within polygon
- `findNearestNeighbors(location, count, filters)`: Find nearest neighbors
- `rangeQuery(center, minRadius, maxRadius, filters)`: Range query between radii
- `intersectionQuery(geometry, filters)`: Find intersecting geometries
- `spatialJoin(dataset1, dataset2, joinOptions)`: Perform spatial join operation
- `clusterLocations(locations, clusterOptions)`: Cluster nearby locations

### GeospatialQueryEngine
- `GeospatialQueryEngine(indexer, optimizer)`: Initialize query engine
- `executeQuery(spatialQuery)`: Execute spatial query
- `optimizeQuery(query)`: Optimize query for performance
- `createQueryPlan(query)`: Create execution plan for query
- `getQueryStatistics(queryId)`: Get query execution statistics
- `validateQuery(query)`: Validate query syntax and parameters
- `explainQuery(query)`: Explain query execution plan
- `cancelQuery(queryId)`: Cancel running query
- `getRunningQueries()`: Get list of running queries
- `setQueryTimeout(timeout)`: Set default query timeout
- `enableQueryLogging(enabled)`: Enable/disable query logging
- `getQueryPerformanceMetrics()`: Get query performance metrics
- `createSpatialIndex(indexDefinition)`: Create new spatial index
- `dropSpatialIndex(indexName)`: Drop spatial index
- `analyzeSpatialDistribution(dataset)`: Analyze spatial data distribution

### ProximityCalculator
- `ProximityCalculator()`: Initialize proximity calculator
- `calculateDistance(location1, location2, distanceType)`: Calculate distance between points
- `calculateDistanceMatrix(locations)`: Calculate distance matrix for locations
- `calculateBearing(from, to)`: Calculate bearing from one point to another
- `calculateMidpoint(location1, location2)`: Calculate midpoint between locations
- `calculateBoundingBox(locations, padding)`: Calculate bounding box for locations
- `isWithinDistance(location1, location2, maxDistance)`: Check if within distance
- `findClosestLocation(target, candidates)`: Find closest location from candidates
- `calculateTravelTime(origin, destination, mode)`: Estimate travel time
- `calculateServiceArea(center, travelTime, mode)`: Calculate service area polygon
- `bufferLocation(location, distance)`: Create buffer around location
- `simplifyGeometry(geometry, tolerance)`: Simplify geometry for performance
- `validateCoordinates(latitude, longitude)`: Validate coordinate values
- `convertCoordinateSystem(location, fromCRS, toCRS)`: Convert coordinate systems
- `getDistanceUnit()`: Get current distance unit setting
- `setDistanceUnit(unit)`: Set distance unit (METER, KILOMETER, MILE, etc.)

## Geofencing API

### GeofencingService
- `GeofencingService(storage, evaluator)`: Initialize geofencing service
- `createGeofence(geofenceDefinition)`: Create new geofence
- `updateGeofence(geofenceId, updates)`: Update geofence configuration
- `deleteGeofence(geofenceId)`: Delete geofence
- `getGeofence(geofenceId)`: Get geofence details
- `getAllGeofences(filters)`: Get all geofences with filtering
- `enableGeofence(geofenceId)`: Enable geofence monitoring
- `disableGeofence(geofenceId)`: Disable geofence monitoring
- `evaluateLocation(userId, location, timestamp)`: Evaluate location against geofences
- `getGeofenceEvents(userId, dateRange)`: Get geofence events for user
- `getGeofenceStatistics(geofenceId)`: Get geofence trigger statistics
- `testGeofence(geofenceId, testLocations)`: Test geofence with sample locations
- `bulkEvaluateLocations(locationUpdates)`: Batch evaluate locations
- `getActiveGeofences(location, radius)`: Get active geofences near location
- `setGeofenceNotification(geofenceId, notificationConfig)`: Configure notifications

### GeofenceManager
- `GeofenceManager(storage, indexer)`: Initialize geofence manager
- `registerGeofence(geofence)`: Register geofence in system
- `unregisterGeofence(geofenceId)`: Unregister geofence
- `indexGeofence(geofence)`: Add geofence to spatial index
- `updateGeofenceIndex(geofenceId, newGeometry)`: Update geofence in index
- `removeFromIndex(geofenceId)`: Remove geofence from index
- `findIntersectingGeofences(location)`: Find geofences intersecting location
- `getGeofencesByUser(userId)`: Get geofences created by user
- `getGeofencesByType(type)`: Get geofences by type
- `validateGeofence(geofence)`: Validate geofence definition
- `optimizeGeofenceSet(geofences)`: Optimize set of geofences
- `mergeOverlappingGeofences(geofences)`: Merge overlapping geofences
- `calculateGeofenceCoverage(area)`: Calculate geofence coverage in area
- `getGeofenceHierarchy()`: Get hierarchical geofence structure
- `setGeofenceMetadata(geofenceId, metadata)`: Set geofence metadata

### GeofenceEvaluator
- `GeofenceEvaluator(config)`: Initialize geofence evaluator
- `evaluatePoint(point, geofences)`: Evaluate point against geofences
- `evaluatePoints(points, geofences)`: Batch evaluate points
- `checkEntry(currentLocation, previousLocation, geofence)`: Check geofence entry
- `checkExit(currentLocation, previousLocation, geofence)`: Check geofence exit
- `checkDwell(userId, geofence, dwellTime)`: Check dwell time in geofence
- `calculateIntersection(path, geofence)`: Calculate path-geofence intersection
- `getEvaluationHistory(userId, geofenceId)`: Get evaluation history
- `setEvaluationRules(rules)`: Set custom evaluation rules
- `getEvaluationRules()`: Get current evaluation rules
- `enableRealTimeEvaluation(enabled)`: Enable real-time evaluation
- `setEvaluationAccuracy(accuracy)`: Set location accuracy threshold
- `getEvaluationMetrics()`: Get evaluation performance metrics
- `optimizeEvaluationOrder(geofences)`: Optimize geofence evaluation order

## Route Planning API

### RoutePlanningService
- `RoutePlanningService(providers, optimizer)`: Initialize route planning service
- `calculateRoute(origin, destination, options)`: Calculate route between points
- `calculateMultiStopRoute(waypoints, options)`: Calculate route with multiple stops
- `optimizeRoute(waypoints, constraints)`: Optimize waypoint order
- `getAlternativeRoutes(origin, destination, count)`: Get alternative route options
- `calculateTravelTime(origin, destination, departureTime)`: Calculate travel time
- `getRouteInstructions(route, instructionOptions)`: Get turn-by-turn instructions
- `updateRouteWithTraffic(route)`: Update route with real-time traffic
- `recalculateRoute(currentRoute, newPosition)`: Recalculate from current position
- `validateRoute(route)`: Validate route feasibility
- `getRouteSummary(route)`: Get route summary statistics
- `exportRoute(route, format)`: Export route in specified format
- `importRoute(routeData, format)`: Import route from external format
- `shareRoute(route, shareOptions)`: Share route with others
- `saveRoute(route, routeName)`: Save route for future use

### RouteOptimizer
- `RouteOptimizer(algorithm, constraints)`: Initialize route optimizer
- `optimizeDeliveryRoute(deliveries, constraints)`: Optimize delivery route
- `optimizeServiceRoute(servicePoints, constraints)`: Optimize service route
- `solveTSP(locations, constraints)`: Solve Traveling Salesman Problem
- `solveVRP(vehicles, customers, constraints)`: Solve Vehicle Routing Problem
- `addConstraint(constraint)`: Add optimization constraint
- `removeConstraint(constraintId)`: Remove optimization constraint
- `setOptimizationCriteria(criteria)`: Set optimization objectives
- `getOptimizationResults(optimizationId)`: Get optimization results
- `getOptimizationStatistics(optimizationId)`: Get optimization statistics
- `cancelOptimization(optimizationId)`: Cancel running optimization
- `setMaxOptimizationTime(timeLimit)`: Set optimization time limit
- `enableRealTimeOptimization(enabled)`: Enable real-time optimization
- `getOptimizationHistory(dateRange)`: Get optimization history
- `validateOptimizationInput(input)`: Validate optimization input

### NavigationService
- `NavigationService(routeService, instructionGenerator)`: Initialize navigation service
- `startNavigation(route, navigationOptions)`: Start navigation session
- `updateNavigationPosition(sessionId, currentLocation)`: Update current position
- `getNextInstruction(sessionId)`: Get next navigation instruction
- `recalculateFromPosition(sessionId, currentLocation)`: Recalculate from position
- `pauseNavigation(sessionId)`: Pause navigation session
- `resumeNavigation(sessionId)`: Resume navigation session
- `stopNavigation(sessionId)`: Stop navigation session
- `getNavigationStatus(sessionId)`: Get navigation session status
- `addWaypoint(sessionId, waypoint)`: Add waypoint to active route
- `removeWaypoint(sessionId, waypointId)`: Remove waypoint from route
- `reportIncident(sessionId, incident)`: Report traffic incident
- `getTrafficAlerts(sessionId)`: Get traffic alerts for route
- `setNavigationPreferences(sessionId, preferences)`: Set navigation preferences
- `getETA(sessionId)`: Get estimated time of arrival
- `getNavigationStatistics(sessionId)`: Get navigation session statistics

## Location Intelligence API

### LocationIntelligenceEngine
- `LocationIntelligenceEngine(analyzers, dataSources)`: Initialize intelligence engine
- `analyzeLocation(location, analysisOptions)`: Analyze location comprehensively
- `getDemographics(location, radius)`: Get demographic data for area
- `getBusinessIntelligence(location, radius)`: Get business intelligence data
- `getFootTrafficPatterns(location, timeRange)`: Get foot traffic patterns
- `getCompetitiveAnalysis(location, businessType)`: Get competitive landscape
- `getPOIData(location, radius, categories)`: Get points of interest data
- `getMarketAnalysis(location, market)`: Get market analysis for location
- `getRealEstateInsights(location, propertyType)`: Get real estate insights
- `getTransportationAccess(location)`: Get transportation accessibility
- `getSafetyMetrics(location)`: Get safety and crime metrics
- `getEnvironmentalData(location)`: Get environmental data
- `generateLocationReport(location, reportOptions)`: Generate comprehensive report
- `compareLocations(locations, comparisonCriteria)`: Compare multiple locations
- `rankLocations(locations, rankingCriteria)`: Rank locations by criteria

### DemographicsAnalyzer
- `DemographicsAnalyzer(dataSources)`: Initialize demographics analyzer
- `getPopulationData(area, resolution)`: Get population data for area
- `getAgeDistribution(area)`: Get age distribution statistics
- `getIncomeDistribution(area)`: Get income distribution data
- `getEducationLevels(area)`: Get education level statistics
- `getEmploymentData(area)`: Get employment and occupation data
- `getHouseholdComposition(area)`: Get household composition data
- `getEthnicityDistribution(area)`: Get ethnicity distribution
- `getLanguageData(area)`: Get language preferences data
- `getConsumerBehavior(area, category)`: Get consumer behavior patterns
- `getPopulationProjections(area, years)`: Get population projections
- `compareAreaDemographics(area1, area2)`: Compare demographics between areas
- `getTargetAudience(criteria)`: Identify target audience locations
- `getMarketSegmentation(area, segmentationModel)`: Get market segments
- `getDemographicTrends(area, timeRange)`: Get demographic trends

### BusinessIntelligenceEngine
- `BusinessIntelligenceEngine(dataSources, models)`: Initialize BI engine
- `getBusinessDensity(area, businessType)`: Get business density metrics
- `getCompetitorAnalysis(location, businessType, radius)`: Analyze competitors
- `getMarketSaturation(area, businessType)`: Calculate market saturation
- `getCustomerFlow(business, timeRange)`: Analyze customer flow patterns
- `getSalesPerformance(business, timeRange)`: Get sales performance data
- `getFootTrafficAnalysis(location, timeRange)`: Analyze foot traffic
- `getSeasonalTrends(business, analysisType)`: Get seasonal business trends
- `getMarketOpportunities(area, businessType)`: Identify market opportunities
- `getSupplyChainAnalysis(business)`: Analyze supply chain efficiency
- `getRealEstateValues(area, propertyType)`: Get real estate value trends
- `getZoningInformation(location)`: Get zoning and land use data
- `getPermitData(area, permitType)`: Get permit and licensing data
- `generateBusinessReport(business, reportType)`: Generate business report

### TrafficAnalyzer
- `TrafficAnalyzer(trafficSources, historicalData)`: Initialize traffic analyzer
- `getCurrentTraffic(area)`: Get current traffic conditions
- `getTrafficPredictions(area, timeWindow)`: Get traffic predictions
- `getHistoricalTraffic(area, timeRange)`: Get historical traffic data
- `analyzeTrafficPatterns(area, analysisOptions)`: Analyze traffic patterns
- `getAccidentData(area, timeRange)`: Get traffic accident data
- `getRoadConditions(area)`: Get road condition information
- `getConstructionImpacts(area)`: Get construction impact data
- `calculateTrafficImpact(incident, affectedArea)`: Calculate traffic impact
- `getOptimalTravelTimes(area)`: Get optimal travel time windows
- `getTrafficHotspots(area)`: Identify traffic congestion hotspots
- `getAlternativeRoutes(blockedRoute)`: Find alternative routes
- `getPublicTransitData(area)`: Get public transportation data
- `generateTrafficReport(area, reportOptions)`: Generate traffic report

## Weather Integration API

### WeatherService
- `WeatherService(weatherProviders, cache)`: Initialize weather service
- `getCurrentWeather(location)`: Get current weather conditions
- `getWeatherForecast(location, days)`: Get weather forecast
- `getWeatherHistory(location, dateRange)`: Get historical weather data
- `getWeatherAlerts(area)`: Get weather alerts and warnings
- `getWeatherRadar(area)`: Get weather radar data
- `getClimateData(location, timeRange)`: Get climate data
- `getSeasonalPatterns(location)`: Get seasonal weather patterns
- `getExtremeWeatherEvents(area, eventType)`: Get extreme weather events
- `correlateWeatherWithLocation(location, weatherFactor)`: Correlate weather impact
- `getWeatherSuitability(location, activity)`: Get weather suitability score
- `getVisibilityData(area)`: Get visibility conditions
- `getUVIndexData(location)`: Get UV index information
- `getAirQualityData(location)`: Get air quality information
- `generateWeatherReport(location, reportOptions)`: Generate weather report

## Analytics and Reporting API

### LocationAnalyticsService
- `LocationAnalyticsService(metricsCollector, reportGenerator)`: Initialize analytics service
- `trackLocationEvent(eventType, locationData)`: Track location-related event
- `getLocationUsageMetrics(dateRange)`: Get location service usage metrics
- `getUserLocationPatterns(userId, analysisOptions)`: Analyze user location patterns
- `getGeofenceEffectiveness(geofenceId, timeRange)`: Analyze geofence effectiveness
- `getRoutePerformanceMetrics(routeType, timeRange)`: Get route performance metrics
- `generateLocationHeatmap(area, metricType, timeRange)`: Generate location heatmap
- `getLocationPopularityTrends(locations, timeRange)`: Get location popularity trends
- `analyzeLocationAccuracy(dataSource, timeRange)`: Analyze location accuracy
- `getServiceQualityMetrics(serviceType, timeRange)`: Get service quality metrics
- `generateInsightReport(reportType, parameters)`: Generate location insights report
- `getLocationROI(businessLocation, timeRange)`: Calculate location ROI
- `analyzeLocationRisk(location, riskFactors)`: Analyze location risk factors
- `predictLocationTrends(location, predictionPeriod)`: Predict location trends

### HeatmapGenerator
- `HeatmapGenerator(dataProcessor, visualizer)`: Initialize heatmap generator
- `generateDensityHeatmap(dataPoints, heatmapOptions)`: Generate density heatmap
- `generateActivityHeatmap(activities, area, timeRange)`: Generate activity heatmap
- `generateTrafficHeatmap(area, timeRange)`: Generate traffic density heatmap
- `generateBusinessHeatmap(businessType, area)`: Generate business density heatmap
- `generateDemographicHeatmap(demographic, area)`: Generate demographic heatmap
- `generateTemporalHeatmap(dataPoints, timeOptions)`: Generate time-based heatmap
- `overlayHeatmaps(heatmaps, overlayOptions)`: Overlay multiple heatmaps
- `animateHeatmap(heatmapData, animationOptions)`: Create animated heatmap
- `exportHeatmap(heatmap, format)`: Export heatmap in various formats
- `getHeatmapStatistics(heatmap)`: Get heatmap statistics
- `optimizeHeatmapResolution(heatmap, targetSize)`: Optimize heatmap resolution
- `applyHeatmapFilters(heatmap, filters)`: Apply filters to heatmap
- `generateHeatmapLegend(heatmap, legendOptions)`: Generate heatmap legend

## REST API Endpoints

### Geocoding Endpoints
- `POST /api/v1/geocode`: Geocode address to coordinates
- `POST /api/v1/geocode/batch`: Batch geocode multiple addresses
- `POST /api/v1/reverse-geocode`: Reverse geocode coordinates to address
- `GET /api/v1/geocode/providers`: Get available geocoding providers
- `GET /api/v1/geocode/cache/stats`: Get geocoding cache statistics
- `DELETE /api/v1/geocode/cache`: Clear geocoding cache

### Location Management Endpoints
- `GET /api/v1/locations`: Get locations with filtering
- `POST /api/v1/locations`: Create new location
- `GET /api/v1/locations/{locationId}`: Get location details
- `PUT /api/v1/locations/{locationId}`: Update location
- `DELETE /api/v1/locations/{locationId}`: Delete location
- `GET /api/v1/locations/search`: Search locations
- `GET /api/v1/locations/nearby`: Find nearby locations
- `GET /api/v1/locations/categories`: Get location categories

### Spatial Query Endpoints
- `POST /api/v1/spatial/radius`: Query locations within radius
- `POST /api/v1/spatial/bounds`: Query locations within bounding box
- `POST /api/v1/spatial/polygon`: Query locations within polygon
- `POST /api/v1/spatial/route`: Query locations along route
- `GET /api/v1/spatial/distance`: Calculate distance between points
- `POST /api/v1/spatial/cluster`: Cluster nearby locations

### Geofencing Endpoints
- `GET /api/v1/geofences`: Get all geofences
- `POST /api/v1/geofences`: Create new geofence
- `GET /api/v1/geofences/{geofenceId}`: Get geofence details
- `PUT /api/v1/geofences/{geofenceId}`: Update geofence
- `DELETE /api/v1/geofences/{geofenceId}`: Delete geofence
- `POST /api/v1/geofences/evaluate`: Evaluate location against geofences
- `GET /api/v1/geofences/{geofenceId}/events`: Get geofence events
- `POST /api/v1/geofences/{geofenceId}/test`: Test geofence

### Route Planning Endpoints
- `POST /api/v1/routes/calculate`: Calculate route between points
- `POST /api/v1/routes/optimize`: Optimize multi-stop route
- `GET /api/v1/routes/{routeId}/instructions`: Get route instructions
- `POST /api/v1/routes/{routeId}/recalculate`: Recalculate route
- `GET /api/v1/routes/alternatives`: Get alternative routes
- `POST /api/v1/navigation/start`: Start navigation session
- `PUT /api/v1/navigation/{sessionId}/position`: Update navigation position
- `POST /api/v1/navigation/{sessionId}/stop`: Stop navigation session

### Location Intelligence Endpoints
- `POST /api/v1/intelligence/analyze`: Analyze location comprehensively
- `GET /api/v1/intelligence/demographics`: Get demographic data
- `GET /api/v1/intelligence/business`: Get business intelligence
- `GET /api/v1/intelligence/traffic`: Get traffic patterns
- `GET /api/v1/intelligence/weather`: Get weather data
- `POST /api/v1/intelligence/compare`: Compare multiple locations
- `POST /api/v1/intelligence/rank`: Rank locations by criteria

### Analytics and Reporting Endpoints
- `GET /api/v1/analytics/usage`: Get service usage analytics
- `GET /api/v1/analytics/patterns`: Get location usage patterns
- `POST /api/v1/analytics/heatmap`: Generate location heatmap
- `GET /api/v1/analytics/trends`: Get location trends
- `POST /api/v1/reports/generate`: Generate location report
- `GET /api/v1/reports/{reportId}`: Get generated report
- `GET /api/v1/reports`: List available reports

### Health and Monitoring Endpoints
- `GET /health`: Service health status
- `GET /health/live`: Liveness probe for Kubernetes
- `GET /health/ready`: Readiness probe for Kubernetes
- `GET /metrics`: Prometheus metrics endpoint
- `GET /api/v1/status`: Service status and uptime
- `GET /api/v1/info`: Service information and version

## Error Handling

### GeoLocationError
- `GeoLocationError(message)`: Create generic geo location error
- `GeoLocationError(message, code)`: Create error with specific code
- `getErrorCode()`: Get error code
- `getErrorDetails()`: Get detailed error information

### GeocodingError
- `GeocodingError(address, message)`: Create geocoding error
- `getAddress()`: Get address that failed geocoding
- `getGeocodingProvider()`: Get provider that failed

### InvalidCoordinatesError
- `InvalidCoordinatesError(latitude, longitude)`: Create invalid coordinates error
- `getLatitude()`: Get invalid latitude value
- `getLongitude()`: Get invalid longitude value

### SpatialQueryError
- `SpatialQueryError(query, message)`: Create spatial query error
- `getQuery()`: Get failed spatial query
- `getQueryType()`: Get type of spatial query

### GeofenceError
- `GeofenceError(geofenceId, message)`: Create geofence error
- `getGeofenceId()`: Get geofence ID from error
- `getGeofenceOperation()`: Get failed geofence operation

### RouteCalculationError
- `RouteCalculationError(origin, destination, message)`: Create route calculation error
- `getOrigin()`: Get route origin from error
- `getDestination()`: Get route destination from error

### LocationNotFoundError
- `LocationNotFoundError(locationId)`: Create location not found error
- `getLocationId()`: Get location ID from error

### ProviderQuotaExceededError
- `ProviderQuotaExceededError(providerId, quotaType)`: Create quota exceeded error
- `getProviderId()`: Get provider ID from error
- `getQuotaType()`: Get exceeded quota type
- `getCurrentUsage()`: Get current usage count
- `getQuotaLimit()`: Get quota limit

### LocationAccuracyError
- `LocationAccuracyError(location, requiredAccuracy, actualAccuracy)`: Create accuracy error
- `getLocation()`: Get location from error
- `getRequiredAccuracy()`: Get required accuracy threshold
- `getActualAccuracy()`: Get actual location accuracy