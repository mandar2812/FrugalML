oldwd <- getwd() 
setwd("/home/mikhail/Desktop/GitProjects/FrugalML/Study") 
files <- list.files() 

evaluations <- read.csv("data/openml_evaluations_all.csv") 
separate_evaluations <- split(evaluations, evaluations$task_id) 

set.seed(101) 

separate_evaluations <- separate_evaluations[-75] 

for (i in 1: length(separate_evaluations)) { 
    
    # make a copy of set
    x <- as.data.frame(separate_evaluations[i])

    # receive name 
    xName <- as.character(x[1, 2]) 

    source("HugeMatrixFunctions.R") 
    
    smallX <- computeTimeAUC(x) 
    
    smallX$CombineTime <- log10(smallX$CombineTime + 1) 
    
    smallX$AUC <- smallX$AUC * -1 
    
    smallX$Algorithm <- sapply(smallX$Algorithm, function(x) shortNameAlgorithmIndividual(as.character(x))) 

    # calculate Pareto front
    paretoFront = smallX[which(!duplicated(cummin(smallX$CombineTime))),] 
    
    # save on disk in png format    
    png(filename= paste("plots/items/", xName, ".png", sep = ""), width = 640, height = 480) 

    plot(paretoFront[,1:2], col = as.factor(paretoFront$Algorithm), xaxt = "n", xlim=c(-1, 0), ylim=c(0, max(paretoFront$CombineTime)), pch = 20, cex = 1.5) 
    axis(1, at = seq(-1, 0, 1/5),  labels = -1 * seq(-1, 0, 1/5))   
    title(main = xName) 
    
    legend("topright", legend = strtrim(paretoFront$Algorithm, 70), col = as.factor(paretoFront$Algorithm), pch = 20, lty = 1, cex = 0.8, pt.cex = 1.2)  
    dev.off() 
} 
         
# store in arff format for external processing  
writeArff <- FALSE 
if (writeArff) {
    library(foreign)
    write.arff(tds, "tactivity.arff", "\n", deparse(substitute(tds))) 
} 

setwd(oldwd) 
