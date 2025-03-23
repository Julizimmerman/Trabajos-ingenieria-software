module Main where

import Palet
import Route
import Stack
import Truck
import Control.Exception
import System.IO.Unsafe

-- | testF returns True if the given action fails (raises an exception), False otherwise.
testF :: Show a => a -> Bool
testF action = unsafePerformIO $ do
    result <- tryJust isException (evaluate action)
    return $ case result of
        Left _ -> True
        Right _ -> False
  where
    isException :: SomeException -> Maybe ()
    isException _ = Just ()

-- | runTest takes a test name and an IO action. It runs the action,
-- catches any exceptions, prints whether the test passed or failed,
-- and then continues with the next test.
runTest :: String -> IO () -> IO ()
runTest testName action = do
    putStrLn ("Running test: " ++ testName)
    result <- try action
    case result of
         Left e  -> putStrLn ("Test \"" ++ testName ++ "\" failed: " ++ show (e :: SomeException))
         Right _ -> putStrLn ("Test \"" ++ testName ++ "\" passed.")

main :: IO ()
main = do
    putStrLn "Running tests..."

    ----------------------------------------------------------------
    -- PALLET TESTS
    runTest "Palet: Valid creation" $ do
         let p1 = newP "CityA" 5
         if destinationP p1 /= "CityA" then error "destinationP failed for valid Palet" else return ()
         if netP p1 /= 5 then error "netP failed for valid Palet" else return ()

    runTest "Palet: newP error on empty destination" $ do
         if not (testF (newP "" 5))
            then error "newP should fail when destination is empty"
            else return ()

    runTest "Palet: newP error on non-positive weight" $ do
         if not (testF (newP "CityA" 0))
            then error "newP should fail when weight is not > 0"
            else return ()

    ----------------------------------------------------------------
    -- ROUTE TESTS
    runTest "Route: inOrderR valid ordering" $ do
         let route = newR ["CityA", "CityB", "CityC"]
         if not (inOrderR route "CityA" "CityB")
            then error "inOrderR failed: 'CityA' should come before 'CityB'"
            else return ()
         if inOrderR route "CityB" "CityA"
            then error "inOrderR failed: 'CityB' should not come before 'CityA'"
            else return ()

    runTest "Route: inOrderR error for missing city" $ do
         let route = newR ["CityA", "CityB", "CityC"]
         if not (testF (inOrderR route "CityX" "CityA"))
            then error "inOrderR should fail if the first city is missing"
            else return ()
         if not (testF (inOrderR route "CityA" "CityX"))
            then error "inOrderR should fail if the second city is missing"
            else return ()

    runTest "Route: newR error on empty list" $ do
         if not (testF (newR []))
            then error "newR should fail when given an empty list"
            else return ()

    ----------------------------------------------------------------
    -- STACK TESTS
    runTest "Stack: freeCellsS and netS" $ do
         let p1 = newP "CityA" 5
         let s = newS 2
         if freeCellsS s /= 2 then error "freeCellsS incorrect on empty stack" else return ()
         let s1 = stackS s p1
         if freeCellsS s1 /= 1 then error "freeCellsS incorrect after one push" else return ()
         if netS s1 /= 5 then error "netS incorrect after one push" else return ()

    runTest "Stack: holdsS valid for empty stack" $ do
         let route = newR ["CityA", "CityB", "CityC"]
         let s = newS 2
         if not (holdsS s (newP "CityA" 3) route)
            then error "holdsS failed for empty stack with valid city"
            else return ()

    runTest "Stack: holdsS valid ordering" $ do
         let route = newR ["CityA", "CityB", "CityC"]
         let s = newS 2
         let s1 = stackS s (newP "CityA" 5)
         let validP = newP "CityB" 4
         if not (holdsS s1 validP route)
            then error "holdsS failed for valid ordering (CityA -> CityB)"
            else return ()

    runTest "Stack: holdsS fails for invalid ordering" $ do
         let route = newR ["CityA", "CityB", "CityC"]
         let s = newS 2
         let s1 = stackS s (newP "CityA" 5)
         let s2 = stackS s1 (newP "CityB" 4)
         if holdsS s2 (newP "CityA" 2) route
            then error "holdsS should fail due to invalid ordering (CityB -> CityA)"
            else return ()

    runTest "Stack: holdsS returns False on full stack" $ do
         let route = newR ["CityA", "CityB", "CityC"]
         let s = newS 2
         let s1 = stackS s (newP "CityA" 5)
         let s2 = stackS s1 (newP "CityB" 4)
         if freeCellsS s2 /= 0 then error "Stack should be full" else return ()
         if holdsS s2 (newP "CityC" 1) route
            then error "holdsS should return False on a full stack"
            else return ()

    runTest "Stack: popS removes correct palets" $ do
         let s = newS 3
         let s3a = stackS s (newP "CityB" 4)
         let s3b = stackS s3a (newP "CityA" 5)
         let s3c = stackS s3b (newP "CityB" 3)
         let s3p = popS s3c "CityB"
         if freeCellsS s3p /= 1 then error "popS did not remove palets correctly (freeCellsS)" else return ()
         if netS s3p /= (5 + 4) then error "popS did not update netS correctly" else return ()

    ----------------------------------------------------------------
    -- TRUCK TESTS
    runTest "Truck: freeCellsT and initial load" $ do
         let truckRoute = newR ["CityA", "CityB", "CityC"]
         let truck = newT 2 2 truckRoute  -- 2 bays with capacity 2 each, total 4 slots
         if freeCellsT truck /= 4 then error "freeCellsT incorrect for new truck" else return ()
         let t1 = loadT truck (newP "CityA" 3)
         if freeCellsT t1 /= 3 then error "freeCellsT incorrect after one load" else return ()
         let t2 = loadT t1 (newP "CityB" 4)
         if netT t2 /= 7 then error "netT incorrect after two loads" else return ()

    runTest "Truck: invalid ordering with single bay" $ do
         let truckRoute = newR ["CityA", "CityB", "CityC"]
         let truckSingle = newT 1 2 truckRoute  -- Only one bay available
         let t1 = loadT truckSingle (newP "CityA" 3)
         let t2 = loadT t1 (newP "CityB" 4)
         if not (testF (loadT t2 (newP "CityA" 2)))
            then error "loadT should fail due to invalid ordering (cannot load CityA after CityB)"
            else return ()

    runTest "Truck: truck capacity" $ do
         let truckRoute = newR ["CityA", "CityB", "CityC"]
         let truck = newT 2 2 truckRoute  -- 2 bahías con capacidad 2 cada una
         let t1 = loadT truck (newP "CityA" 3)
         let t2 = loadT t1 (newP "CityB" 4)
         let t3 = loadT t2 (newP "CityC" 2)
         -- No podemos cargar un palet para CityA después de haber cargado para CityB y CityC
         -- debido a las restricciones de ordenamiento
         if not (testF (loadT t3 (newP "CityA" 1)))
            then error "loadT debería fallar al intentar cargar un palet con destino anterior en la ruta"
            else return ()
         -- Pero sí podemos cargar otro palet para CityC
         let t4 = loadT t3 (newP "CityC" 1)
         -- Y verificamos que falle cuando el camión está lleno
         if freeCellsT t4 /= 0 then error "El camión debería tener celdas disponibles" else return ()
         if not (testF (loadT t4 (newP "CityC" 1)))
            then error "loadT debería fallar cuando el camión está lleno"
            else return ()

    runTest "Truck: unloadT removes palets" $ do
         let truckRoute = newR ["CityA", "CityB", "CityC"]
         let truck2 = newT 2 3 truckRoute
         putStrLn "Camión inicial: "
         print truck2
         
         let truck2a = loadT truck2 (newP "CityA" 2)
         putStrLn "Después de cargar CityA (2 ton): "
         print truck2a
         putStrLn $ "Peso neto: " ++ show (netT truck2a)
         
         let truck2b = loadT truck2a (newP "CityB" 3)
         putStrLn "Después de cargar CityB (3 ton): "
         print truck2b
         putStrLn $ "Peso neto: " ++ show (netT truck2b)
         
         let truck2c = loadT truck2b (newP "CityA" 4)
         putStrLn "Después de cargar CityA (4 ton): "
         print truck2c
         putStrLn $ "Peso neto: " ++ show (netT truck2c)
         
         let truck2u = unloadT truck2c "CityA"
         putStrLn "Después de descargar CityA: "
         print truck2u
         putStrLn $ "Peso neto final: " ++ show (netT truck2u)
         
         if netT truck2u /= 3 
            then error $ "unloadT did not unload palets correctly (netT mismatch). Expected: 3, Got: " ++ show (netT truck2u)
            else return ()

    putStrLn "\nAll tests completed!"
