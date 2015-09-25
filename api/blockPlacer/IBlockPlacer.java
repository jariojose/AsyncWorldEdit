/*
 * AsyncWorldEdit Premium is a commercial version of AsyncWorldEdit. This software 
 * has been sublicensed by the software original author according to p7 of
 * AsyncWorldEdit license.
 *
 * AsyncWorldEdit Premium - donation version of AsyncWorldEdit, a performance 
 * improvement plugin for Minecraft WorldEdit plugin.
 *
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
 *
 * All rights reserved.
 *
 * 1. You may: 
 *    install and use AsyncWorldEdit in accordance with the Software documentation
 *    and pursuant to the terms and conditions of this license
 * 2. You may not:
 *    sell, redistribute, encumber, give, lend, rent, lease, sublicense, or otherwise
 *    transfer Software, or any portions of Software, to anyone without the prior 
 *    written consent of Licensor
 * 3. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 4. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.asyncworldedit.api.blockPlacer;

import com.sk89q.worldedit.MaxChangedBlocksException;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerEntry;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerPlayer;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.utils.FuncParamEx;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;
import org.primesoft.asyncworldedit.worldedit.ThreadSafeEditSession;

/**
 *
 * @author SBPrime
 */
public interface IBlockPlacer {

    /**
     * Add new job for player
     *
     * @param player player UUID
     * @param job the job
     * @return
     */
    boolean addJob(IPlayerEntry player, JobEntry job);

    /**
     * Add event listener
     *
     * @param listener
     */
    void addListener(IBlockPlacerListener listener);

    /**
     * Add task to perform in async mode
     *
     * @param player
     * @param entry
     * @return
     */
    boolean addTasks(IPlayerEntry player, BlockPlacerEntry entry);

    /**
     * Cancel job
     *
     * @param player
     * @param jobId
     * @return
     */
    int cancelJob(IPlayerEntry player, int jobId);

    /**
     * Get all players in log
     *
     * @return players list
     */
    IPlayerEntry[] getAllPlayers();

    /**
     * Get the player job
     *
     * @param player player uuid
     * @param jobId job ID
     * @return
     */
    JobEntry getJob(IPlayerEntry player, int jobId);

    /**
     * Get next job id for player
     *
     * @param player
     * @return
     */
    int getJobId(IPlayerEntry player);

    /**
     * Gets the number of events for a player
     *
     * @param player player login
     * @return number of stored events
     */
    BlockPlacerPlayer getPlayerEvents(IPlayerEntry player);

    /**
     * Is the blocks placer paused
     * @return
     */
    boolean isPaused();

    /**
     * Wrap action into AsyncWorldEdit job and perform it asynchronicly
     *
     * @param editSession
     * @param player
     * @param jobName
     * @param action
     */
    void performAsAsyncJob(final ThreadSafeEditSession editSession, final IPlayerEntry player, final String jobName, final FuncParamEx<Integer, CancelabeEditSession, MaxChangedBlocksException> action);

    /**
     * Remove all entries for player
     *
     * @param player
     * @return
     */
    int purge(IPlayerEntry player);

    /**
     * Remove all entries
     *
     * @return Number of purged job entries
     */
    int purgeAll();

    /**
     * Remove the player job
     *
     * @param player
     * @param jobEntry
     */
    void removeJob(final IPlayerEntry player, JobEntry jobEntry);

    /**
     * Remove event listener
     *
     * @param listener
     */
    void removeListener(IBlockPlacerListener listener);

    /**
     * Set pause on blocks placer
     * @param pause
     */
    void setPause(boolean pause);
    
}
