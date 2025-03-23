import Palet
import Route
import Stack
import Truck
import Control.Exception
import System.IO.Unsafe (unsafePerformIO)
import Control.Arrow (Arrow(first))

-- Función de testeo que ya te dieron
testF :: Show a => a -> Bool
testF action = unsafePerformIO $ do
    result <- tryJust isException (evaluate action)
    return $ case result of
        Left _ -> True
        Right _ -> False
    where
        isException :: SomeException -> Maybe ()
        isException _ = Just ()

-- genero una ruta para generar Stacks y Trucks
route :: Route
route = newR ["City1", "City2", "City3", "City4"]

 -- genero diversos Palets 
p1 :: Palet
p1 = newP "City1" 1
p2 :: Palet
p2 = newP "City2" 2
p3 :: Palet
p3 = newP "City3" 3
p4 :: Palet
p4 = newP "City4" 4

firstStack :: Stack
firstStack = newS 3 -- capacity 3

firstTruck :: Truck 
firstTruck = newT 2 3 route -- 2 stacks de capacidad 3

-- Test Palet 
testPalet :: [Bool]
testPalet = [
    destinationP p1 == "City1",
    netP p2 == 2,
    testF (newP "" 1), -- city vacia 
    testF (newP "City1" 0) -- peso no positivo
    ]

-- Test Route
testRoute :: [Bool]
testRoute = [
    inOrderR route "City1" "City2",
    not (inOrderR route "City3" "City2"),
    not (inOrderR route "City4" "City4"), -- misma ciudad 
    testF (inOrderR route "City1" "City5"), -- ciudad no existente
    testF (newR []), -- ruta vacia
    testF (inOrderR route "" "City2"), -- primera ciudad vacia
    testF (inOrderR route "City1" "") -- segunda ciudad vacia
    ]

-- Test Stack
testStack :: [Bool]
testStack = [
    testF( stackS firstStack p1), -- agregar palet a un stack vacio 
    freeCellsS (stackS firstStack p1) == 2, -- test freeCellsS 
    freeCellsS (stackS (stackS (stackS firstStack p1) p2) p3) == 0, -- stack que se llena 
    testF (stackS (stackS (stackS (stackS firstStack p1) p2) p3) p4), -- stack lleno
    holdsS firstStack p1 route, -- stack vacio
    holdsS (stackS firstStack p1) p2 route, -- stack con un palet
    not (holdsS (stackS firstStack p1) p3 route), -- ciudad incorrecta
    not (holdsS (stackS firstStack p1) p2 (newR ["City1", "City3", "City2", "City4"])), -- orden incorrecto
    holdsS (stackS firstStack p1) p2 (newR ["City1", "City2", "City3", "City4"]) -- orden correcto
    ]



-- Función de testeo
runTests :: IO ()
runTests = do
    putStrLn "==== TEST PALET ===="
    print testPalet
    putStrLn "==== TEST ROUTE ===="
    print testRoute
    putStrLn "==== TEST STACK ===="
    print testStack
    putStrLn "==== TEST TRUCK FULL CYCLE ===="


