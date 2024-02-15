* Circuit updates now happen in the scheduled tick phase of the world, instead of the block entity ticks. Circuits might have to be manually block-updated in case they stop running.
* Fixed circuits having wrong delays for scheduled ticks in some cases
