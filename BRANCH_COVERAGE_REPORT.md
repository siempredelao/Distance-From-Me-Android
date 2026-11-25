# Branch Coverage Report
**Project**: Distance From Me Android

**Method**: JaCoCo 0.8.12 over `./gradlew testDebugUnitTest`, measured on 2026-11-25. The build doesn't run JaCoCo, so these numbers only change when someone measures again. "Tests" counts each case of a parameterized test.

JaCoCo counts branches in the bytecode, so some belong to code the compiler adds, for example for coroutines, and a test may not be able to reach them. "none" means a class has no branches.

## Summary
| Component | Classes | With tests | Tests | Branches | Lines |
|---|---|---|---|---|---|
| UseCases | 9 | 9 | 25 | 4/4 (100%) | 24/25 (96%) |
| Repositories | 9 | 9 | 43 | 19/21 (90%) | 88/90 (97%) |
| Mappers | 11 | 11 | 56 | 60/60 (100%) | 161/162 (99%) |
| Formatters | 2 | 2 | 14 | 2/2 (100%) | 10/10 (100%) |
| ViewModels | 7 | 7 | 122 | 126/149 (84%) | 522/540 (96%) |
| Other domain classes | 3 | 3 | 75 | 50/56 (89%) | 61/63 (96%) |
| **Total** | **41** | **41** | **335** | **261/292 (89%)** | **866/890 (97%)** |

## UseCases
| Class | Tests | Branches | Lines |
|---|---|---|---|
| ClearDistancesUseCase | 2 | none | 2/2 (100%) |
| GetAddressCoordinatesByNameUseCase | 3 | none | 2/2 (100%) |
| GetAddressNameByCoordinatesUseCase | 3 | none | 2/2 (100%) |
| GetDistancesUseCase | 2 | none | 2/2 (100%) |
| GetElevationByCoordinatesUseCase | 6 | 4/4 (100%) | 4/4 (100%) |
| GetOpenSourceLibrariesUseCase | 2 | none | 2/2 (100%) |
| GetPositionListUseCase | 3 | none | 2/2 (100%) |
| GetStoredDistancesUseCase | 2 | none | 6/7 (85%) |
| SaveDistanceUseCase | 2 | none | 2/2 (100%) |

## Repositories
| Class | Tests | Branches | Lines |
|---|---|---|---|
| BaseAddressRepository | 3 | none | 5/5 (100%) |
| BaseCoordinatesRepository | 5 | none | 5/5 (100%) |
| BaseDistanceModeRepository | 2 | none | 5/5 (100%) |
| BaseDistanceRepository | 5 | 2/2 (100%) | 10/10 (100%) |
| BaseElevationRepository | 2 | none | 4/4 (100%) |
| BaseHintRepository | 5 | 2/2 (100%) | 9/9 (100%) |
| BaseLocationRepository | 7 | 7/9 (77%) | 28/30 (93%) |
| BaseOpenSourceRepository | 1 | none | 5/5 (100%) |
| BaseSettingsRepository | 13 | 8/8 (100%) | 17/17 (100%) |

## Mappers
| Class | Tests | Branches | Lines |
|---|---|---|---|
| AddressCollectionEntityDataMapper | 8 | 8/8 (100%) | 12/12 (100%) |
| DistanceEntityMapper | 4 | 2/2 (100%) | 7/7 (100%) |
| ElevationEntityDataMapper | 8 | 9/9 (100%) | 11/11 (100%) |
| GeocodingErrorMessageMapper | 6 | 5/5 (100%) | 9/9 (100%) |
| LicenseMapper | 5 | 4/4 (100%) | 7/7 (100%) |
| LoadDistancesMapper | 2 | 2/2 (100%) | 18/18 (100%) |
| MapStateMapper | 9 | 30/30 (100%) | 56/56 (100%) |
| OpenSourceLibraryMapper | 7 | none | 11/11 (100%) |
| OpenSourceLibraryUiMapper | 1 | none | 13/14 (92%) |
| PositionEntityMapper | 3 | none | 5/5 (100%) |
| ShareInfoMessageMapper | 3 | none | 12/12 (100%) |

## Formatters
| Class | Tests | Branches | Lines |
|---|---|---|---|
| AddressFormatter | 4 | 2/2 (100%) | 3/3 (100%) |
| UnitFormatter | 10 | none | 7/7 (100%) |

## ViewModels
| Class | Tests | Branches | Lines |
|---|---|---|---|
| AddressViewModel | 9 | 12/12 (100%) | 46/54 (85%) |
| ElevationViewModel | 16 | 12/12 (100%) | 33/33 (100%) |
| MainViewModel | 63 | 55/75 (73%) | 265/273 (97%) |
| OpenSourceViewModel | 5 | 8/8 (100%) | 18/18 (100%) |
| SaveDistanceViewModel | 5 | 4/4 (100%) | 33/33 (100%) |
| SettingsViewModel | 2 | none | 12/14 (85%) |
| ShowInfoViewModel | 22 | 35/38 (92%) | 115/115 (100%) |

## Other domain classes
| Class | Tests | Branches | Lines |
|---|---|---|---|
| MapHintProcessor | 27 | 32/36 (88%) | 27/29 (93%) |
| UnitConverter | 25 | 14/16 (87%) | 12/12 (100%) |
| DistanceCalculator | 23 | 4/4 (100%) | 22/22 (100%) |

## Missed branches
- `MainViewModel` (20): among them the `catch` in `onLoadDistancesClick`, which no test runs, and one to three branches on each of 14 other lines.
- `MapHintProcessor` (4), `ShowInfoViewModel` (3) and `UnitConverter` (2): each is one branch of a line whose others are covered.
- `BaseLocationRepository` (2): on the line that opens its `callbackFlow`.
