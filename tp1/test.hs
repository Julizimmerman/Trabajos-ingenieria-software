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
p5 :: Palet
p5 = newP "City5" 4
p6 :: Palet
p6 = newP "City6" 6
paletPesado :: Palet
paletPesado = newP "City5" 11  -- Palet que por sí solo excede las 10 toneladas

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
    freeCellsS (stackS firstStack p1) == 2, -- test freeCellsS 
    freeCellsS (stackS (stackS (stackS firstStack p1) p2) p3) == 0, -- stack que se llena 
    testF (stackS (stackS (stackS (stackS firstStack p1) p2) p3) p4), -- stack lleno
    holdsS firstStack p1 route, -- stack vacio
    not (holdsS (stackS firstStack p1) p2 route), -- chequeo orden de ciudades 
    holdsS (stackS firstStack p3) p1 route, 
    not (holdsS (stackS firstStack p1) p2 (newR ["City1", "City3", "City2", "City4"])), -- orden incorrecto
    
    -- Casos de fallos por peso
    testF (stackS (stackS (stackS firstStack p1) p2) paletPesado), -- excede peso
    testF (stackS firstStack paletPesado),  -- palet solo excede peso máximo

    -- Casos límite de peso justo
    freeCellsS (stackS (stackS firstStack p5) p6) == 1,  -- Capacidad 3, queda 1 libre
    netS (stackS (stackS firstStack p5) p6) == 10,       -- Peso justo en el límite

    -- Capacidad llena pero peso ok
    freeCellsS (stackS (stackS (stackS firstStack p1) p2) p3) == 0,
    netS (stackS (stackS (stackS firstStack p1) p2) p3) < 10,
    testF (stackS(stackS (stackS (stackS firstStack p1) p2) p3) p4), -- falla por capacidad llena aunque peso esté bien

    -- popS eliminando palet con destino correcto
    let st1 = stackS (stackS (stackS firstStack p1) p2) p3
    in netS (popS st1 "City2") == netS st1 - netP p2,

    -- popS sobre pila vacía no cambia nada
    popS firstStack "City1" == firstStack,


    -- holdsS falla si el orden de entrega es incorrecto
    let routeRara = newR ["City3", "City2", "City1"]
        st2 = stackS firstStack p2
    in not (holdsS st2 p1 routeRara),


    -- Chequeo apilar palet de peso 0 (si se permite)
    let paletCero = newP "City1" 0
    in testF(freeCellsS (stackS firstStack paletCero) == 2)
    ]

truck2 :: Truck
truck2 = loadT (loadT (loadT firstTruck p1) p2) p3    

-- Test Truck
testTruck :: [Bool]
testTruck = [
    freeCellsT firstTruck == 6, -- 2 bahías de capacidad 3, total 6 espacios
    freeCellsT (loadT firstTruck p1) == 5, -- Se carga un palet en el camión
    freeCellsT (loadT (loadT firstTruck p1) p2) == 4, -- Se carga otro palet en el camión
    testF (loadT firstTruck (newP "City5" 2)), -- Palet con ciudad no en la ruta
    netT (unloadT (loadT firstTruck p1) "City1") == 0, -- Descargar el único palet en la ciudad 1
    freeCellsT (unloadT (loadT firstTruck p1) "City1") == 6, -- Después de descargar, vuelve a su capacidad inicial
    let truck2 = loadT (loadT (loadT firstTruck p1) p2) p3 in
        testF(netT (unloadT truck2 "City2") == 3), -- Después de descargar en City2, queda el palet de City3
    testF(netT (unloadT (unloadT truck2 "City2") "City3") == 0) -- Después de City3, no quedan palets
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
    putStrLn "==== TEST TRUCK ===="
    print testTruck


