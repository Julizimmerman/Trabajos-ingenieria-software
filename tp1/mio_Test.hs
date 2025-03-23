import Palet
import Route
import Stack
import Truck
import Control.Exception
import System.IO.Unsafe (unsafePerformIO)

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

-- Datos compartidos
rTest :: Route
rTest = newR ["roma", "paris", "mdq", "berna"]

pRoma :: Palet
pRoma = newP "roma" 3
pParis :: Palet
pParis = newP "paris" 4
pMdq :: Palet
pMdq = newP "mdq" 3
pPesado :: Palet
pPesado = newP "roma" 11

stackVacio :: Stack
stackVacio = newS 3 -- capacity = 3 
stackCargado :: Stack
stackCargado = stackS (stackS stackVacio pRoma) pParis

camionBase :: Truck
camionBase = newT 2 3 rTest


-- Test Palet
testPalet :: [Bool]
testPalet = [
    destinationP pRoma == "roma",
    netP pParis == 4,
    netP pMdq == 3
    ]

-- Test Route
testRoute :: [Bool]
testRoute = [
    inOrderR rTest "roma" "paris",
    inOrderR rTest "paris" "berna",
    not (inOrderR rTest "mdq" "roma"),
    not (inOrderR rTest "berna" "berna") 
    ]

-- Test Stack
testStack :: [Bool]
testStack = [
    freeCellsS stackVacio == 3,
    freeCellsS stackCargado == 1,
    netS stackCargado == 7,
    holdsS stackCargado pMdq rTest, 
    testF (stackS stackVacio pPesado),   -- Exceso de peso
    netS (popS stackCargado "roma") == 4
    ]

-- Test Truck
testTruck :: [Bool]
testTruck =
    let c1 = loadT camionBase pRoma
        c2 = loadT c1 pParis
    in [
        freeCellsT camionBase == 6,
        freeCellsT c1 == 5,
        netT c1 == 3,
        netT c2 == 7,
        netT (unloadT c2 "roma") == 4
    ]


main :: IO ()
main = do
    putStrLn "==== TEST PALET ===="
    print testPalet
    putStrLn $ if and testPalet then "✔️ Palet OK" else "Palet FALLA"

    putStrLn "==== TEST ROUTE ===="
    print testRoute
    putStrLn $ if and testRoute then "✔️ Route OK" else "Route FALLA"

    putStrLn "==== TEST STACK ===="
    print testStack
    putStrLn $ if and testStack then "✔️ Stack OK" else "Stack FALLA"

    putStrLn "==== TEST TRUCK ===="
    print testTruck
    putStrLn $ if and testTruck then "✔️ Truck OK" else "Truck FALLA"
